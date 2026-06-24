<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>上传图片 - 深度可视化景深检测</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <script src="https://cdnjs.cloudflare.com/ajax/libs/gsap/3.12.5/gsap.min.js"></script>
</head>
<body>
    <div class="page-header">
        <div class="container">
            <div class="header-content">
                <a href="${pageContext.request.contextPath}/" class="logo">深度可视化景深检测</a>
                <div class="nav-links">
                    <a href="${pageContext.request.contextPath}/">上传图片</a>
                    <a href="${pageContext.request.contextPath}/HistoryServlet">历史记录</a>
                </div>
            </div>
        </div>
    </div>

    <div class="upload-container">
        <div class="container">
            <div class="upload-section" id="upload-section">
                <h1 id="upload-title">上传图片进行景深分析</h1>
                <p id="upload-desc">支持多种图片格式，自动识别景深区域并进行色彩可视化</p>
            </div>

            <div class="drop-zone" id="drop-zone">
                <div class="drop-zone-icon">📷</div>
                <h3>拖拽图片到这里</h3>
                <p>或者点击选择文件</p>
                <input type="file" id="file-input" accept=".jpg,.jpeg,.png,.tiff,.raw" multiple>
                <div class="supported-formats">
                    <span class="format-tag">JPG</span>
                    <span class="format-tag">PNG</span>
                    <span class="format-tag">TIFF</span>
                    <span class="format-tag">RAW</span>
                </div>
            </div>

            <div class="progress-container" id="progress-container">
                <div class="progress-bar">
                    <div class="progress-fill" id="progress-fill"></div>
                </div>
                <p style="color: rgba(255,255,255,0.6); text-align: center; margin-top: 10px;" id="progress-text">上传中...</p>
            </div>
        </div>
    </div>

    <script>
        window.contextPath = '${pageContext.request.contextPath}';
    </script>
    <script src="${pageContext.request.contextPath}/js/upload.js"></script>

    <script>
        document.addEventListener('DOMContentLoaded', function() {
            gsap.from('#upload-title', {
                opacity: 0,
                y: 30,
                duration: 0.8,
                ease: 'power3.out',
                overwrite: true
            });

            gsap.from('#upload-desc', {
                opacity: 0,
                y: 20,
                duration: 0.6,
                delay: 0.3,
                ease: 'power2.out',
                overwrite: true
            });

            gsap.from('#drop-zone', {
                opacity: 0,
                scale: 0.95,
                duration: 0.8,
                delay: 0.5,
                ease: 'power3.out',
                overwrite: true,
                onComplete: function() {
                    document.getElementById('drop-zone').style.opacity = '1';
                    document.getElementById('drop-zone').style.transform = 'scale(1)';
                }
            });

            gsap.from('.supported-formats', {
                opacity: 0,
                y: 20,
                duration: 0.6,
                delay: 0.8,
                ease: 'power2.out',
                overwrite: true
            });
        });
    </script>
</body>
</html>