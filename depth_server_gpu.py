import torch
import torch.nn.functional as F
from PIL import Image
import numpy as np
from flask import Flask, request, jsonify, send_file
from flask_cors import CORS
import os
import io
import sys

app = Flask(__name__)
CORS(app)

class LayerNorm(torch.nn.Module):
    def __init__(self, normalized_shape, eps=1e-6):
        super().__init__()
        self.weight = torch.nn.Parameter(torch.ones(normalized_shape))
        self.bias = torch.nn.Parameter(torch.zeros(normalized_shape))
        self.eps = eps

    def forward(self, x):
        u = x.mean(-1, keepdim=True)
        s = (x - u).pow(2).mean(-1, keepdim=True)
        x = (x - u) / torch.sqrt(s + self.eps)
        return self.weight * x + self.bias

class Attention(torch.nn.Module):
    def __init__(self, dim, num_heads=12, qkv_bias=True):
        super().__init__()
        self.num_heads = num_heads
        head_dim = dim // num_heads
        self.scale = head_dim ** -0.5
        self.qkv = torch.nn.Linear(dim, dim * 3, bias=qkv_bias)
        self.proj = torch.nn.Linear(dim, dim)

    def forward(self, x):
        B, N, C = x.shape
        qkv = self.qkv(x).reshape(B, N, 3, self.num_heads, C // self.num_heads).permute(2, 0, 3, 1, 4)
        q, k, v = qkv[0], qkv[1], qkv[2]
        attn = (q @ k.transpose(-2, -1)) * self.scale
        attn = attn.softmax(dim=-1)
        x = (attn @ v).transpose(1, 2).reshape(B, N, C)
        x = self.proj(x)
        return x

class MLP(torch.nn.Module):
    def __init__(self, in_features, hidden_features=None, out_features=None, act_layer=torch.nn.GELU):
        super().__init__()
        out_features = out_features or in_features
        hidden_features = hidden_features or in_features
        self.fc1 = torch.nn.Linear(in_features, hidden_features)
        self.act = act_layer()
        self.fc2 = torch.nn.Linear(hidden_features, out_features)

    def forward(self, x):
        x = self.fc1(x)
        x = self.act(x)
        x = self.fc2(x)
        return x

class Block(torch.nn.Module):
    def __init__(self, dim, num_heads):
        super().__init__()
        self.norm1 = LayerNorm(dim)
        self.attn = Attention(dim, num_heads=num_heads)
        self.ls1 = torch.nn.Parameter(torch.ones(dim))
        self.norm2 = LayerNorm(dim)
        self.mlp = MLP(in_features=dim, hidden_features=dim * 4)
        self.ls2 = torch.nn.Parameter(torch.ones(dim))

    def forward(self, x):
        x = x + self.ls1 * self.attn(self.norm1(x))
        x = x + self.ls2 * self.mlp(self.norm2(x))
        return x

class VisionTransformer(torch.nn.Module):
    def __init__(self, img_size=518, patch_size=14, embed_dim=768, depth=12, num_heads=12):
        super().__init__()
        self.patch_size = patch_size
        self.patch_embed = torch.nn.Module()
        self.patch_embed.proj = torch.nn.Conv2d(3, embed_dim, kernel_size=patch_size, stride=patch_size)
        self.num_patches = (img_size // patch_size) * (img_size // patch_size)
        self.cls_token = torch.nn.Parameter(torch.zeros(1, 1, embed_dim))
        self.mask_token = torch.nn.Parameter(torch.zeros(1, 1, 768))
        self.pos_embed = torch.nn.Parameter(torch.zeros(1, self.num_patches + 1, embed_dim))
        self.blocks = torch.nn.ModuleList([Block(dim=embed_dim, num_heads=num_heads) for _ in range(depth)])
        self.norm = LayerNorm(embed_dim)

    def forward(self, x):
        B, C, H, W = x.shape
        x = self.patch_embed.proj(x).flatten(2).transpose(1, 2)
        cls_tokens = self.cls_token.expand(B, -1, -1)
        x = torch.cat((cls_tokens, x), dim=1)
        x = x + self.pos_embed
        for blk in self.blocks:
            x = blk(x)
        x = self.norm(x)
        return x

class ResidualConvUnit(torch.nn.Module):
    def __init__(self, features):
        super().__init__()
        self.conv1 = torch.nn.Conv2d(features, features, kernel_size=3, stride=1, padding=1, bias=True)
        self.conv2 = torch.nn.Conv2d(features, features, kernel_size=3, stride=1, padding=1, bias=True)
        self.relu = torch.nn.ReLU(True)

    def forward(self, x):
        out = self.relu(x)
        out = self.conv1(out)
        out = self.relu(out)
        out = self.conv2(out)
        return out + x

class RefineNetBlock(torch.nn.Module):
    def __init__(self, features):
        super().__init__()
        self.resConfUnit1 = ResidualConvUnit(features)
        self.resConfUnit2 = ResidualConvUnit(features)
        self.out_conv = torch.nn.Conv2d(features, features, kernel_size=1, stride=1, bias=True)

    def forward(self, x):
        out = self.resConfUnit1(x)
        out = self.resConfUnit2(out)
        out = self.out_conv(out)
        return out

class DepthHead(torch.nn.Module):
    def __init__(self):
        super().__init__()
        self.projects = torch.nn.ModuleList([
            torch.nn.Conv2d(768, 96, kernel_size=1, stride=1, bias=True),
            torch.nn.Conv2d(768, 192, kernel_size=1, stride=1, bias=True),
            torch.nn.Conv2d(768, 384, kernel_size=1, stride=1, bias=True),
            torch.nn.Conv2d(768, 768, kernel_size=1, stride=1, bias=True),
        ])
        self.resize_layers = torch.nn.ModuleList([
            torch.nn.ConvTranspose2d(96, 96, kernel_size=4, stride=2, padding=1, bias=True),
            torch.nn.ConvTranspose2d(192, 192, kernel_size=2, stride=2, padding=0, bias=True),
            torch.nn.ConvTranspose2d(384, 384, kernel_size=2, stride=2, padding=0, bias=True),
            torch.nn.ConvTranspose2d(768, 768, kernel_size=3, stride=2, padding=1, bias=True),
        ])
        self.scratch = torch.nn.Module()
        self.scratch.layer1_rn = torch.nn.Conv2d(96, 128, kernel_size=3, stride=1, padding=1, bias=False)
        self.scratch.layer2_rn = torch.nn.Conv2d(192, 128, kernel_size=3, stride=1, padding=1, bias=False)
        self.scratch.layer3_rn = torch.nn.Conv2d(384, 128, kernel_size=3, stride=1, padding=1, bias=False)
        self.scratch.layer4_rn = torch.nn.Conv2d(768, 128, kernel_size=3, stride=1, padding=1, bias=False)
        self.scratch.refinenet1 = RefineNetBlock(128)
        self.scratch.refinenet2 = RefineNetBlock(128)
        self.scratch.refinenet3 = RefineNetBlock(128)
        self.scratch.refinenet4 = RefineNetBlock(128)
        self.scratch.output_conv1 = torch.nn.Conv2d(128, 64, kernel_size=3, stride=1, padding=1, bias=True)
        self.scratch.output_conv2 = torch.nn.ModuleList([
            torch.nn.ConvTranspose2d(64, 32, kernel_size=3, stride=2, padding=1, bias=True),
            None,
            torch.nn.Conv2d(32, 1, kernel_size=1, stride=1, bias=True),
        ])

    def forward(self, x):
        layer_1, layer_2, layer_3, layer_4 = x
        
        layer_1 = self.projects[0](layer_1)
        layer_2 = self.projects[1](layer_2)
        layer_3 = self.projects[2](layer_3)
        layer_4 = self.projects[3](layer_4)
        
        layer_1 = self.resize_layers[0](layer_1)
        layer_2 = self.resize_layers[1](layer_2)
        layer_3 = self.resize_layers[2](layer_3)
        layer_4 = self.resize_layers[3](layer_4)
        
        target_h, target_w = layer_1.shape[-2:]
        layer_2 = F.interpolate(layer_2, (target_h, target_w), mode='bilinear', align_corners=True)
        layer_3 = F.interpolate(layer_3, (target_h, target_w), mode='bilinear', align_corners=True)
        layer_4 = F.interpolate(layer_4, (target_h, target_w), mode='bilinear', align_corners=True)
        
        layer_1_rn = self.scratch.layer1_rn(layer_1)
        layer_2_rn = self.scratch.layer2_rn(layer_2)
        layer_3_rn = self.scratch.layer3_rn(layer_3)
        layer_4_rn = self.scratch.layer4_rn(layer_4)
        
        path_4 = self.scratch.refinenet4(layer_4_rn)
        path_3 = self.scratch.refinenet3(layer_3_rn + path_4)
        path_2 = self.scratch.refinenet2(layer_2_rn + path_3)
        path_1 = self.scratch.refinenet1(layer_1_rn + path_2)
        
        out = self.scratch.output_conv1(path_1)
        if self.scratch.output_conv2[0] is not None:
            out = self.scratch.output_conv2[0](out)
        if self.scratch.output_conv2[2] is not None:
            out = self.scratch.output_conv2[2](out)
        
        return out

class DepthAnythingV2(torch.nn.Module):
    def __init__(self):
        super().__init__()
        self.pretrained = VisionTransformer()
        self.depth_head = DepthHead()

    def forward(self, x):
        B, C, H, W = x.shape
        features = []
        def hook_fn(module, input, output):
            features.append(output)
        handles = []
        handles.append(self.pretrained.blocks[2].register_forward_hook(hook_fn))
        handles.append(self.pretrained.blocks[5].register_forward_hook(hook_fn))
        handles.append(self.pretrained.blocks[8].register_forward_hook(hook_fn))
        handles.append(self.pretrained.blocks[11].register_forward_hook(hook_fn))
        _ = self.pretrained(x)
        for h in handles:
            h.remove()
        
        layer_1 = features[0][:, 1:, :].transpose(1, 2).reshape(B, 768, H // 14, W // 14)
        layer_2 = features[1][:, 1:, :].transpose(1, 2).reshape(B, 768, H // 14, W // 14)
        layer_3 = features[2][:, 1:, :].transpose(1, 2).reshape(B, 768, H // 14, W // 14)
        layer_4 = features[3][:, 1:, :].transpose(1, 2).reshape(B, 768, H // 14, W // 14)
        
        return self.depth_head([layer_1, layer_2, layer_3, layer_4])

model = None
device = None

def load_model(model_path):
    global model, device
    if torch.backends.mps.is_available():
        device = torch.device('mps')
        print(f"✅ Using Apple Silicon MPS GPU", file=sys.stderr)
    elif torch.cuda.is_available():
        device = torch.device('cuda')
        print(f"✅ Using CUDA GPU", file=sys.stderr)
    else:
        device = torch.device('cpu')
        print(f"⚠️  Using CPU", file=sys.stderr)
    model = DepthAnythingV2()
    state_dict = torch.load(model_path, map_location='cpu', weights_only=True)
    model_dict = model.state_dict()
    new_state_dict = {}
    for k, v in state_dict.items():
        new_k = k
        if 'ls1.gamma' in k:
            new_k = k.replace('.ls1.gamma', '.ls1')
        if 'ls2.gamma' in k:
            new_k = k.replace('.ls2.gamma', '.ls2')
        if new_k in model_dict:
            if model_dict[new_k].shape == v.shape:
                new_state_dict[new_k] = v
            else:
                if new_k == 'pretrained.mask_token' and v.shape == (1, 768):
                    new_state_dict[new_k] = v.unsqueeze(0)
    model.load_state_dict(new_state_dict, strict=False)
    model = model.to(device)
    model.eval()
    for param in model.parameters():
        param.requires_grad = False
    print("✅ Model loaded!", file=sys.stderr)

@app.route('/predict', methods=['POST'])
def predict():
    if 'image' not in request.files:
        return jsonify({'error': 'No image provided'}), 400
    file = request.files['image']
    try:
        image = Image.open(file.stream).convert('RGB')
        width, height = image.size
        input_size = 518
        img = image.resize((input_size, input_size), Image.BICUBIC)
        img = torch.from_numpy(np.array(img)).permute(2, 0, 1).float().unsqueeze(0).to(device)
        img = (img / 255.0 - torch.tensor([0.485, 0.456, 0.406]).to(device).view(1, 3, 1, 1)) / torch.tensor([0.229, 0.224, 0.225]).to(device).view(1, 3, 1, 1)
        with torch.no_grad():
            depth = model(img)
        depth = F.interpolate(depth, (height, width), mode='bilinear', align_corners=True)
        depth = depth.squeeze().cpu().numpy()
        depth_normalized = (depth - depth.min()) / (depth.max() - depth.min() + 1e-8)
        depth_image = (depth_normalized * 255).astype(np.uint8)
        depth_pil = Image.fromarray(depth_image, mode='L').convert('RGB')
        buffer = io.BytesIO()
        depth_pil.save(buffer, format='PNG')
        buffer.seek(0)
        return send_file(buffer, mimetype='image/png')
    except Exception as e:
        print(f"Error: {e}", file=sys.stderr)
        return jsonify({'error': str(e)}), 500

@app.route('/predict_data', methods=['POST'])
def predict_data():
    if 'image' not in request.files:
        return jsonify({'error': 'No image provided'}), 400
    file = request.files['image']
    try:
        image = Image.open(file.stream).convert('RGB')
        width, height = image.size
        input_size = 518
        img = image.resize((input_size, input_size), Image.BICUBIC)
        img = torch.from_numpy(np.array(img)).permute(2, 0, 1).float().unsqueeze(0).to(device)
        img = (img / 255.0 - torch.tensor([0.485, 0.456, 0.406]).to(device).view(1, 3, 1, 1)) / torch.tensor([0.229, 0.224, 0.225]).to(device).view(1, 3, 1, 1)
        with torch.no_grad():
            depth = model(img)
        depth = F.interpolate(depth, (height, width), mode='bilinear', align_corners=True)
        depth = depth.squeeze().cpu().numpy()
        depth_normalized = (depth - depth.min()) / (depth.max() - depth.min() + 1e-8)
        depth_data = depth_normalized.tolist()
        return jsonify({'width': width, 'height': height, 'depth_data': depth_data})
    except Exception as e:
        print(f"Error: {e}", file=sys.stderr)
        return jsonify({'error': str(e)}), 500

@app.route('/')
def index():
    return jsonify({
        'status': 'ok',
        'message': 'Depth Anything V2 GPU Accelerated Service',
        'device': str(device),
        'endpoints': {
            '/predict': 'POST - Returns depth map PNG',
            '/predict_data': 'POST - Returns depth data JSON',
            '/health': 'GET - Health check'
        }
    })

@app.route('/health', methods=['GET'])
def health():
    return jsonify({'status': 'ok', 'device': str(device), 'gpu_available': torch.backends.mps.is_available() or torch.cuda.is_available()})

if __name__ == '__main__':
    model_path = '/Users/emptychentairan/Downloads/depth_anything_v2_metric_vkitti_vitb.pth'
    load_model(model_path)
    app.run(host='0.0.0.0', port=5001, debug=False)
