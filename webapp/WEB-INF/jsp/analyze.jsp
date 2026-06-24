<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>景深分析 - Depth Anything V2</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <script src="https://cdnjs.cloudflare.com/ajax/libs/gsap/3.12.5/gsap.min.js"></script>
</head>
<body>
    <div class="page-header">
        <div class="container">
            <div class="header-content">
                <a href="${pageContext.request.contextPath}/" class="logo">Depth Anything V2</a>
                <div class="nav-links">
                    <a href="${pageContext.request.contextPath}/">上传图片</a>
                    <a href="${pageContext.request.contextPath}/HistoryServlet">历史记录</a>
                </div>
            </div>
        </div>
    </div>

    <div class="analyze-container">
        <div class="container">
            <div class="analyze-header">
                <h2 id="analyze-title">深度估计分析</h2>
                <a href="${pageContext.request.contextPath}/" class="back-btn">返回上传</a>
            </div>

            <div class="compare-wrapper" id="compare-wrapper">
                <div class="compare-panel original-panel">
                    <div class="panel-header">
                        <h3>📷 原图</h3>
                        <button class="panel-download-btn" onclick="downloadOriginal()">下载原图</button>
                    </div>
                    <div class="canvas-wrapper">
                        <img id="original-image" alt="原图">
                    </div>
                </div>

                <div class="compare-panel depth-panel">
                    <div class="panel-header">
                        <h3>🎯 深度图</h3>
                        <button class="panel-download-btn" onclick="downloadDepthImage()">下载深度图</button>
                    </div>
                    <div class="canvas-wrapper">
                        <canvas id="depth-canvas"></canvas>
                    </div>
                </div>

                <div class="compare-panel visual-panel">
                    <div class="panel-header">
                        <h3>🎨 可视化效果</h3>
                        <button class="panel-download-btn" onclick="downloadVisualImage()">下载可视化图</button>
                    </div>
                    <div class="canvas-wrapper">
                        <canvas id="visual-canvas"></canvas>
                    </div>
                </div>
            </div>

            <div class="controls-panel" id="controls-panel">
                <h3>🎚️ 参数调节</h3>
                
                <div class="control-group">
                    <label for="near-threshold">前景阈值 <span id="near-value">60</span></label>
                    <input type="range" id="near-threshold" class="control-slider" min="0" max="127" value="60">
                </div>

                <div class="control-group">
                    <label for="far-threshold">远景阈值 <span id="far-value">180</span></label>
                    <input type="range" id="far-threshold" class="control-slider" min="128" max="255" value="180">
                </div>

                <div class="control-group">
                    <label for="mask-opacity">蒙版透明度 <span id="opacity-value">40%</span></label>
                    <input type="range" id="mask-opacity" class="control-slider" min="10" max="100" value="40">
                </div>

                <div style="display: flex; flex-wrap: wrap;">
                    <button id="analyze-btn" class="analyze-btn">开始分析</button>
                    <button id="download-both-btn" class="download-btn">下载全部</button>
                </div>

                <div class="legend">
                    <div class="legend-item">
                        <div class="legend-color near"></div>
                        <span>前景区域 (近)</span>
                    </div>
                    <div class="legend-item">
                        <div class="legend-color focus"></div>
                        <span>合焦区域 (中)</span>
                    </div>
                    <div class="legend-item">
                        <div class="legend-color far"></div>
                        <span>远景区域 (远)</span>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <input type="hidden" id="img-id" value="${requestScope.imgId}">
    <input type="hidden" id="save-name" value="${requestScope.saveName}">

    <script>
        var contextPath = '${pageContext.request.contextPath}';
        var depthData = null;

        document.addEventListener('DOMContentLoaded', function() {
            gsap.from('#analyze-title', {
                opacity: 0,
                y: 20,
                duration: 0.6,
                ease: 'power3.out'
            });

            gsap.from('#compare-wrapper', {
                opacity: 0,
                scale: 0.95,
                duration: 0.8,
                delay: 0.2,
                ease: 'power3.out'
            });

            gsap.from('#controls-panel', {
                opacity: 0,
                y: 30,
                duration: 0.8,
                delay: 0.4,
                ease: 'power3.out'
            });

            const canvas = document.getElementById('depth-canvas');
            const visualCanvas = document.getElementById('visual-canvas');
            const ctx = canvas.getContext('2d');
            const visualCtx = visualCanvas.getContext('2d');
            const originalImg = document.getElementById('original-image');
            const imgId = document.getElementById('img-id').value;
            const saveName = document.getElementById('save-name').value;
            
            const nearThresholdSlider = document.getElementById('near-threshold');
            const farThresholdSlider = document.getElementById('far-threshold');
            const opacitySlider = document.getElementById('mask-opacity');
            const nearValue = document.getElementById('near-value');
            const farValue = document.getElementById('far-value');
            const opacityValue = document.getElementById('opacity-value');
            
            const analyzeBtn = document.getElementById('analyze-btn');
            const downloadBothBtn = document.getElementById('download-both-btn');

            let originalImageObj = null;
            let nearThreshold = 60;
            let farThreshold = 180;
            let maskOpacity = 0.4;

            nearThresholdSlider.addEventListener('input', function() {
                nearThreshold = parseInt(this.value);
                nearValue.textContent = nearThreshold;
                if (originalImageObj && depthData) {
                    renderDepthVisual();
                }
            });

            farThresholdSlider.addEventListener('input', function() {
                farThreshold = parseInt(this.value);
                farValue.textContent = farThreshold;
                if (originalImageObj && depthData) {
                    renderDepthVisual();
                }
            });

            opacitySlider.addEventListener('input', function() {
                maskOpacity = parseInt(this.value) / 100;
                opacityValue.textContent = this.value + '%';
                if (originalImageObj && depthData) {
                    renderDepthVisual();
                }
            });

            function loadOriginalImage() {
                if (!imgId) {
                    showToast('缺少图片ID', 'error');
                    return;
                }

                const img = new Image();
                img.crossOrigin = 'anonymous';
                img.onload = function() {
                    originalImageObj = img;
                    canvas.width = img.width;
                    canvas.height = img.height;
                    visualCanvas.width = img.width;
                    visualCanvas.height = img.height;
                    
                    const maxDisplayWidth = 400;
                    const maxDisplayHeight = 500;
                    let displayWidth = img.width;
                    let displayHeight = img.height;
                    
                    if (displayWidth > maxDisplayWidth) {
                        displayHeight = Math.round(displayHeight * (maxDisplayWidth / displayWidth));
                        displayWidth = maxDisplayWidth;
                    }
                    if (displayHeight > maxDisplayHeight) {
                        displayWidth = Math.round(displayWidth * (maxDisplayHeight / displayHeight));
                        displayHeight = maxDisplayHeight;
                    }
                    
                    canvas.style.width = displayWidth + 'px';
                    canvas.style.height = displayHeight + 'px';
                    visualCanvas.style.width = displayWidth + 'px';
                    visualCanvas.style.height = displayHeight + 'px';
                    originalImg.style.width = displayWidth + 'px';
                    originalImg.style.height = displayHeight + 'px';
                    
                    ctx.drawImage(img, 0, 0);
                    visualCtx.drawImage(img, 0, 0);
                };
                img.onerror = function() {
                    showToast('图片加载失败', 'error');
                };
                img.src = contextPath + '/uploadImg/' + saveName;
                originalImg.src = contextPath + '/uploadImg/' + saveName;
            }

            function fetchDepthMap() {
                showToast('正在调用 Depth Anything V2 模型进行深度估计...', 'success');
                
                const formData = new FormData();
                const img = new Image();
                img.crossOrigin = 'anonymous';
                img.onload = function() {
                    const tempCanvas = document.createElement('canvas');
                    tempCanvas.width = img.width;
                    tempCanvas.height = img.height;
                    const tempCtx = tempCanvas.getContext('2d');
                    tempCtx.drawImage(img, 0, 0);
                    
                    tempCanvas.toBlob(function(blob) {
                        formData.append('image', blob, 'image.jpg');
                        
                        fetch('http://localhost:5001/predict', {
                            method: 'POST',
                            body: formData
                        })
                        .then(response => response.blob())
                        .then(blob => {
                            const depthImg = new Image();
                            depthImg.onload = function() {
                                const depthCanvas = document.createElement('canvas');
                                depthCanvas.width = depthImg.width;
                                depthCanvas.height = depthImg.height;
                                const depthCtx = depthCanvas.getContext('2d');
                                depthCtx.drawImage(depthImg, 0, 0);
                                
                                const imageData = depthCtx.getImageData(0, 0, depthCanvas.width, depthCanvas.height);
                                const data = imageData.data;
                                depthData = [];
                                
                                for (let y = 0; y < depthCanvas.height; y++) {
                                    depthData[y] = [];
                                    for (let x = 0; x < depthCanvas.width; x++) {
                                        const index = (y * depthCanvas.width + x) * 4;
                                        depthData[y][x] = data[index];
                                    }
                                }
                                
                                renderDepthMap();
                                renderDepthVisual();
                                showToast('深度估计完成!', 'success');
                            };
                            depthImg.src = URL.createObjectURL(blob);
                        })
                        .catch(error => {
                            console.error('深度估计失败:', error);
                            showToast('深度估计服务不可用，使用本地算法', 'warning');
                            generateLocalDepthMap();
                        });
                    }, 'image/jpeg', 0.9);
                };
                img.src = contextPath + '/uploadImg/' + saveName;
            }

            function generateLocalDepthMap() {
                depthData = [];
                for (let y = 0; y < canvas.height; y++) {
                    depthData[y] = [];
                    for (let x = 0; x < canvas.width; x++) {
                        const gray = calculateGrayValue(x, y);
                        const blurred = applyBlur(x, y, gray);
                        depthData[y][x] = Math.min(255, Math.max(0, 255 - blurred));
                    }
                }
                renderDepthMap();
                renderDepthVisual();
            }

            function calculateGrayValue(x, y) {
                const imageData = ctx.getImageData(x, y, 1, 1);
                const r = imageData.data[0];
                const g = imageData.data[1];
                const b = imageData.data[2];
                return Math.round(0.299 * r + 0.587 * g + 0.114 * b);
            }

            function applyBlur(x, y, centerValue) {
                let sum = centerValue;
                let count = 1;
                const radius = 5;
                
                for (let dy = -radius; dy <= radius; dy++) {
                    for (let dx = -radius; dx <= radius; dx++) {
                        if (dx === 0 && dy === 0) continue;
                        const nx = x + dx;
                        const ny = y + dy;
                        if (nx >= 0 && nx < canvas.width && ny >= 0 && ny < canvas.height) {
                            const gray = calculateGrayValue(nx, ny);
                            const weight = Math.exp(-(dx * dx + dy * dy) / (2 * radius * radius));
                            sum += gray * weight;
                            count += weight;
                        }
                    }
                }
                return sum / count;
            }

            function renderDepthMap() {
                if (!depthData) return;

                const imageData = ctx.createImageData(canvas.width, canvas.height);
                const data = imageData.data;

                for (let y = 0; y < canvas.height; y++) {
                    for (let x = 0; x < canvas.width; x++) {
                        const index = (y * canvas.width + x) * 4;
                        const val = depthData[y] ? depthData[y][x] : 128;
                        data[index] = val;
                        data[index + 1] = val;
                        data[index + 2] = val;
                        data[index + 3] = 255;
                    }
                }

                ctx.putImageData(imageData, 0, 0);
            }

            function renderDepthVisual() {
                if (!originalImageObj || !depthData) return;

                visualCtx.drawImage(originalImageObj, 0, 0);

                const imageData = visualCtx.getImageData(0, 0, visualCanvas.width, visualCanvas.height);
                const data = imageData.data;

                for (let y = 0; y < visualCanvas.height; y++) {
                    for (let x = 0; x < visualCanvas.width; x++) {
                        const index = (y * visualCanvas.width + x) * 4;
                        
                        let depthValue = 128;
                        if (depthData[y] && depthData[y][x] !== undefined) {
                            depthValue = depthData[y][x];
                        }
                        
                        let r, g, b;

                        if (depthValue <= nearThreshold) {
                            r = 255;
                            g = 100;
                            b = 100;
                        } else if (depthValue <= farThreshold) {
                            r = 100;
                            g = 255;
                            b = 100;
                        } else {
                            r = 100;
                            g = 150;
                            b = 255;
                        }

                        data[index] = Math.round(data[index] * (1 - maskOpacity) + r * maskOpacity);
                        data[index + 1] = Math.round(data[index + 1] * (1 - maskOpacity) + g * maskOpacity);
                        data[index + 2] = Math.round(data[index + 2] * (1 - maskOpacity) + b * maskOpacity);
                    }
                }

                visualCtx.putImageData(imageData, 0, 0);
            }

            analyzeBtn.addEventListener('click', function() {
                gsap.to(analyzeBtn, {
                    scale: 0.95,
                    duration: 0.1,
                    yoyo: true,
                    repeat: 1
                });

                if (!originalImageObj) {
                    loadOriginalImage();
                    setTimeout(function() {
                        if (originalImageObj) {
                            fetchDepthMap();
                            callAnalyzeAPI();
                        }
                    }, 500);
                } else {
                    fetchDepthMap();
                    callAnalyzeAPI();
                }
            });

            function callAnalyzeAPI() {
                fetch(contextPath + '/DepthAnalyzeServlet', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/x-www-form-urlencoded'
                    },
                    body: 'imgId=' + encodeURIComponent(imgId) + '&nearThreshold=' + nearThreshold + '&farThreshold=' + farThreshold + '&maskOpacity=' + maskOpacity
                })
                .then(response => response.json())
                .then(data => {
                    if (data.success) {
                        showToast('分析完成', 'success');
                    } else {
                        showToast(data.message || '分析失败', 'error');
                    }
                })
                .catch(error => {
                    showToast('分析请求失败', 'error');
                });
            }

            window.downloadOriginal = function() {
                const url = contextPath + '/uploadImg/' + saveName;
                forceDownload(url, 'original_' + saveName);
            };

            window.downloadDepthImage = function() {
                if (!depthData) {
                    showToast('请先进行深度估计', 'error');
                    return;
                }

                gsap.to(downloadBothBtn, {
                    scale: 0.95,
                    duration: 0.1,
                    yoyo: true,
                    repeat: 1
                });

                const link = document.createElement('a');
                link.download = 'depth_map_' + Date.now() + '.png';
                link.href = document.getElementById('depth-canvas').toDataURL('image/png');
                link.click();
                
                showToast('深度图下载成功', 'success');
            };

            window.downloadVisualImage = function() {
                if (!depthData) {
                    showToast('请先进行深度估计', 'error');
                    return;
                }

                const link = document.createElement('a');
                link.download = 'depth_visualization_' + Date.now() + '.png';
                link.href = document.getElementById('visual-canvas').toDataURL('image/png');
                link.click();
                
                showToast('可视化图下载成功', 'success');
            };

            downloadBothBtn.addEventListener('click', function() {
                downloadOriginal();
                setTimeout(() => {
                    downloadDepthImage();
                }, 500);
                setTimeout(() => {
                    downloadVisualImage();
                }, 1000);
            });

            async function forceDownload(url, filename) {
                try {
                    const blob = await fetchAsBlob(url);
                    const objUrl = URL.createObjectURL(blob);
                    const a = document.createElement('a');
                    a.href = objUrl;
                    a.download = filename;
                    document.body.appendChild(a);
                    a.click();
                    document.body.removeChild(a);
                    setTimeout(() => URL.revokeObjectURL(objUrl), 1000);
                    showToast('文件 ' + filename + ' 下载成功', 'success');
                } catch (error) {
                    showToast('下载失败: ' + error.message, 'error');
                }
            }

            async function fetchAsBlob(url, timeoutMs = 30000) {
                const controller = new AbortController();
                const timeout = setTimeout(() => controller.abort(), timeoutMs);
                
                try {
                    const response = await fetch(url, { signal: controller.signal });
                    if (!response.ok) {
                        throw new Error('网络请求失败: ' + response.status);
                    }
                    return await response.blob();
                } finally {
                    clearTimeout(timeout);
                }
            }

            function showToast(message, type) {
                const toast = document.createElement('div');
                toast.className = `toast ${type}`;
                toast.textContent = message;
                document.body.appendChild(toast);

                gsap.to(toast, {
                    x: 0,
                    duration: 0.3,
                    ease: 'power2.out'
                });

                setTimeout(function() {
                    gsap.to(toast, {
                        x: '100%',
                        duration: 0.3,
                        ease: 'power2.in',
                        onComplete: function() {
                            toast.remove();
                        }
                    });
                }, 3000);
            }

            loadOriginalImage();
        });
    </script>
</body>
</html>