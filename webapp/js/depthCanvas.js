class DepthVisualizer {
    constructor(canvasId) {
        this.canvas = document.getElementById(canvasId);
        this.ctx = this.canvas.getContext('2d');
        this.originalImage = null;
        this.nearThreshold = 60;
        this.farThreshold = 180;
        this.maskOpacity = 0.4;
    }

    loadImage(src) {
        return new Promise((resolve, reject) => {
            const img = new Image();
            img.crossOrigin = 'anonymous';
            img.onload = () => {
                this.originalImage = img;
                this.canvas.width = img.width;
                this.canvas.height = img.height;
                this.ctx.drawImage(img, 0, 0);
                resolve();
            };
            img.onerror = () => {
                reject(new Error('图片加载失败'));
            };
            img.src = src;
        });
    }

    setThresholds(near, far) {
        this.nearThreshold = near;
        this.farThreshold = far;
        this.render();
    }

    setOpacity(opacity) {
        this.maskOpacity = opacity;
        this.render();
    }

    render() {
        if (!this.originalImage) return;

        this.ctx.drawImage(this.originalImage, 0, 0);

        const imageData = this.ctx.getImageData(0, 0, this.canvas.width, this.canvas.height);
        const data = imageData.data;

        for (let y = 0; y < this.canvas.height; y++) {
            for (let x = 0; x < this.canvas.width; x++) {
                const index = (y * this.canvas.width + x) * 4;
                
                const depthValue = this.calculateDepthValue(x, y);
                let r, g, b;

                if (depthValue <= this.nearThreshold) {
                    r = 255;
                    g = 100;
                    b = 100;
                } else if (depthValue <= this.farThreshold) {
                    r = 100;
                    g = 255;
                    b = 100;
                } else {
                    r = 100;
                    g = 150;
                    b = 255;
                }

                data[index] = Math.round(data[index] * (1 - this.maskOpacity) + r * this.maskOpacity);
                data[index + 1] = Math.round(data[index + 1] * (1 - this.maskOpacity) + g * this.maskOpacity);
                data[index + 2] = Math.round(data[index + 2] * (1 - this.maskOpacity) + b * this.maskOpacity);
            }
        }

        this.ctx.putImageData(imageData, 0, 0);
    }

    calculateDepthValue(x, y) {
        const normalizedX = x / this.canvas.width;
        const normalizedY = y / this.canvas.height;
        const distance = Math.sqrt(normalizedX * normalizedX + normalizedY * normalizedY);
        return Math.min(Math.round(distance * 255), 255);
    }

    download(fileName) {
        const link = document.createElement('a');
        link.download = fileName || 'depth_visualization.png';
        link.href = this.canvas.toDataURL('image/png');
        link.click();
    }

    getImageData() {
        return this.ctx.getImageData(0, 0, this.canvas.width, this.canvas.height);
    }
}