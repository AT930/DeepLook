var contextPath = '';

document.addEventListener('DOMContentLoaded', function() {
    if (window.contextPath) {
        contextPath = window.contextPath;
    } else {
        var pathname = window.location.pathname;
        var match = pathname.match(/^\/[^/]+/);
        contextPath = match ? match[0] : '';
    }
    
    const dropZone = document.getElementById('drop-zone');
    const fileInput = document.getElementById('file-input');
    const progressContainer = document.getElementById('progress-container');
    const progressFill = document.getElementById('progress-fill');
    const progressText = document.getElementById('progress-text');

    const SUPPORTED_FORMATS = ['jpg', 'jpeg', 'png', 'tiff', 'raw'];
    const MAX_FILE_SIZE = 50 * 1024 * 1024;
    const MAX_WIDTH = 2048;
    const MAX_HEIGHT = 2048;

    dropZone.addEventListener('dragover', function(e) {
        e.preventDefault();
        dropZone.classList.add('dragover');
        
        gsap.to(dropZone, {
            scale: 1.02,
            duration: 0.3,
            ease: 'power2.out'
        });
    });

    dropZone.addEventListener('dragleave', function(e) {
        e.preventDefault();
        dropZone.classList.remove('dragover');
        
        gsap.to(dropZone, {
            scale: 1,
            duration: 0.3,
            ease: 'power2.out'
        });
    });

    dropZone.addEventListener('drop', function(e) {
        e.preventDefault();
        dropZone.classList.remove('dragover');
        
        gsap.to(dropZone, {
            scale: 1,
            duration: 0.3,
            ease: 'power2.out'
        });

        const files = e.dataTransfer.files;
        if (files.length > 0) {
            handleFiles(files);
        }
    });

    fileInput.addEventListener('change', function(e) {
        const files = e.target.files;
        if (files.length > 0) {
            handleFiles(files);
        }
    });

    function handleFiles(files) {
        const validFiles = [];
        
        for (let file of files) {
            const ext = file.name.split('.').pop().toLowerCase();
            
            if (!SUPPORTED_FORMATS.includes(ext)) {
                showToast('文件 ' + file.name + ' 格式不支持', 'error');
                continue;
            }
            
            if (file.size > MAX_FILE_SIZE) {
                showToast('文件 ' + file.name + ' 超过大小限制（50MB）', 'error');
                continue;
            }
            
            validFiles.push(file);
        }

        if (validFiles.length === 0) {
            return;
        }

        if (validFiles.length < files.length) {
            showToast('已过滤 ' + (files.length - validFiles.length) + ' 个文件', 'error');
        }

        uploadFiles(validFiles);
    }

    function compressImage(file) {
        return new Promise((resolve) => {
            const reader = new FileReader();
            
            reader.onload = function(e) {
                const img = new Image();
                
                img.onload = function() {
                    let width = img.width;
                    let height = img.height;
                    
                    if (width > MAX_WIDTH) {
                        height = Math.round(height * (MAX_WIDTH / width));
                        width = MAX_WIDTH;
                    }
                    
                    if (height > MAX_HEIGHT) {
                        width = Math.round(width * (MAX_HEIGHT / height));
                        height = MAX_HEIGHT;
                    }
                    
                    const canvas = document.createElement('canvas');
                    canvas.width = width;
                    canvas.height = height;
                    
                    const ctx = canvas.getContext('2d');
                    ctx.drawImage(img, 0, 0, width, height);
                    
                    canvas.toBlob(function(blob) {
                        if (blob) {
                            const compressedFile = new File([blob], file.name, {
                                type: 'image/jpeg',
                                lastModified: Date.now()
                            });
                            resolve(compressedFile);
                        } else {
                            resolve(file);
                        }
                    }, 'image/jpeg', 0.85);
                };
                
                img.onerror = function() {
                    resolve(file);
                };
                
                img.src = e.target.result;
            };
            
            reader.onerror = function() {
                resolve(file);
            };
            
            reader.readAsDataURL(file);
        });
    }

    async function uploadFiles(files) {
        progressContainer.style.display = 'block';
        progressFill.style.width = '0%';
        progressText.textContent = '准备处理...';

        const totalFiles = files.length;
        let processedCount = 0;

        for (let i = 0; i < files.length; i++) {
            const file = files[i];
            
            progressText.textContent = '正在处理图片 ' + (i + 1) + '/' + totalFiles;
            
            const compressedFile = await compressImage(file);
            processedCount++;
            progressFill.style.width = (processedCount / totalFiles * 50) + '%';

            const result = await uploadSingleFile(compressedFile, processedCount, totalFiles);
            if (result && result.success && result.redirectUrl) {
                window.location.href = result.redirectUrl;
                return;
            }
        }
    }

    function uploadSingleFile(file, currentIndex, totalFiles) {
        return new Promise((resolve) => {
            const formData = new FormData();
            formData.append('image', file);

            const xhr = new XMLHttpRequest();

            xhr.upload.addEventListener('progress', function(e) {
                if (e.lengthComputable) {
                    const fileProgress = (e.loaded / e.total) * 100;
                    const baseProgress = ((currentIndex - 1) / totalFiles) * 100;
                    const totalProgress = baseProgress + (fileProgress / totalFiles) * 50;
                    progressFill.style.width = totalProgress + '%';
                    progressText.textContent = '上传中 ' + Math.round(totalProgress) + '%';
                }
            });

            xhr.addEventListener('load', function() {
                progressFill.style.width = '100%';
                progressText.textContent = '处理完成';

                console.log('Status:', xhr.status);
                console.log('Response Type:', xhr.responseType);
                console.log('Response Text:', xhr.responseText);
                console.log('Content-Type:', xhr.getResponseHeader('Content-Type'));

                try {
                    const response = JSON.parse(xhr.responseText);
                    if (response.success && response.redirectUrl) {
                        resolve(response);
                    } else {
                        showToast(response.message || '上传失败', 'error');
                        resolve(null);
                    }
                } catch (e) {
                    console.error('JSON Parse Error:', e);
                    console.error('Raw Response:', xhr.responseText);
                    showToast('响应解析错误: ' + (xhr.responseText || '空响应'), 'error');
                    resolve(null);
                }
            });

            xhr.addEventListener('error', function() {
                showToast('网络错误', 'error');
                resolve(null);
            });

            xhr.open('POST', contextPath + '/UploadServlet');
            xhr.send(formData);
        });
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
});