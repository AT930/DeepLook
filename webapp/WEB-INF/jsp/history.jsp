<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>历史记录 - 深度可视化景深检测</title>
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

    <div class="history-container">
        <div class="container">
            <div class="history-header">
                <h2 id="history-title">历史记录</h2>
                <a href="${pageContext.request.contextPath}/" class="upload-link">上传新图片</a>
            </div>

            <div class="history-grid" id="history-grid">
                <c:if test="${empty images}">
                    <div style="grid-column: 1/-1; text-align: center; padding: 60px;">
                        <div style="font-size: 64px; color: rgba(255,255,255,0.3); margin-bottom: 20px;">📭</div>
                        <p style="color: rgba(255,255,255,0.6); font-size: 18px;">暂无历史记录</p>
                        <p style="color: rgba(255,255,255,0.4); font-size: 14px; margin-top: 10px;">上传图片进行景深分析后会保存在这里</p>
                    </div>
                </c:if>

                <c:forEach var="image" items="${images}" varStatus="status">
                    <div class="history-card" id="card-${status.index}">
                        <img src="${pageContext.request.contextPath}/uploadImg/${image.saveName}" 
                             alt="${image.originName}" 
                             class="card-image"
                             onclick="viewImage('${pageContext.request.contextPath}/uploadImg/${image.saveName}')">
                        <div class="card-info">
                            <div class="card-name">${image.originName}</div>
                            <div class="card-meta">
                                <span>${image.fileSize} KB</span>
                                <span>${image.uploadTime}</span>
                            </div>
                            <div class="card-actions">
                                <button class="action-btn view" onclick="viewAnalyze(${image.imgId})">查看分析</button>
                                <button class="action-btn download" onclick="downloadImage('${pageContext.request.contextPath}/uploadImg/${image.saveName}', '${image.originName}')">下载</button>
                                <button class="action-btn delete" onclick="deleteImage(${image.imgId}, this)">删除</button>
                            </div>
                        </div>
                    </div>
                </c:forEach>
            </div>

            <c:if test="${total > 0}">
                <div class="pagination">
                    <button class="pagination-btn" onclick="goToPage(${pageNum - 1})" ${pageNum <= 1 ? 'disabled' : ''}>上一页</button>
                    <span class="pagination-info">第 ${pageNum} / ${totalPages} 页</span>
                    <button class="pagination-btn" onclick="goToPage(${pageNum + 1})" ${pageNum >= totalPages ? 'disabled' : ''}>下一页</button>
                </div>
            </c:if>
        </div>
    </div>

    <div class="modal" id="image-modal">
        <button class="modal-close" onclick="closeModal()">×</button>
        <div class="modal-content">
            <img id="modal-image" src="" alt="预览">
        </div>
    </div>

    <script>
        var contextPath = '${pageContext.request.contextPath}';
        document.addEventListener('DOMContentLoaded', function() {
            gsap.from('#history-title', {
                opacity: 0,
                y: 20,
                duration: 0.6,
                ease: 'power3.out'
            });

            gsap.from('.history-card', {
                opacity: 0,
                y: 30,
                duration: 0.5,
                stagger: 0.1,
                delay: 0.3,
                ease: 'power2.out'
            });

            gsap.from('.pagination', {
                opacity: 0,
                y: 20,
                duration: 0.5,
                delay: 0.6,
                ease: 'power2.out'
            });
        });

        function viewImage(src) {
            const modal = document.getElementById('image-modal');
            const modalImage = document.getElementById('modal-image');
            modalImage.src = src;
            modal.classList.add('show');
            
            gsap.from(modal, {
                opacity: 0,
                duration: 0.3,
                ease: 'power2.out'
            });
            
            gsap.from('.modal-content', {
                scale: 0.9,
                opacity: 0,
                duration: 0.3,
                ease: 'power2.out'
            });
        }

        function closeModal() {
            const modal = document.getElementById('image-modal');
            
            gsap.to('.modal-content', {
                scale: 0.9,
                opacity: 0,
                duration: 0.2,
                ease: 'power2.in'
            });
            
            gsap.to(modal, {
                opacity: 0,
                duration: 0.2,
                delay: 0.1,
                ease: 'power2.in',
                onComplete: function() {
                    modal.classList.remove('show');
                }
            });
        }

        function viewAnalyze(imgId) {
            window.location.href = contextPath + '/DepthAnalyzeServlet?imgId=' + imgId;
        }

        function downloadImage(src, fileName) {
            const link = document.createElement('a');
            link.href = src;
            link.download = fileName;
            document.body.appendChild(link);
            link.click();
            document.body.removeChild(link);
            
            showToast('下载成功', 'success');
        }

        function deleteImage(imgId, btn) {
            if (!confirm('确定要删除这张图片吗？')) {
                return;
            }

            gsap.to(btn.closest('.history-card'), {
                scale: 0.9,
                opacity: 0.5,
                duration: 0.2,
                ease: 'power2.out'
            });

            fetch(contextPath + '/HistoryServlet', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded'
                },
                body: 'action=delete&imgId=' + imgId
            })
            .then(response => response.json())
            .then(data => {
                if (data.success) {
                    const card = btn.closest('.history-card');
                    gsap.to(card, {
                        y: 30,
                        opacity: 0,
                        duration: 0.3,
                        ease: 'power2.in',
                        onComplete: function() {
                            card.remove();
                            showToast('删除成功', 'success');
                            
                            const grid = document.getElementById('history-grid');
                            if (grid.children.length === 0) {
                                grid.innerHTML = `
                                    <div style="grid-column: 1/-1; text-align: center; padding: 60px;">
                                        <div style="font-size: 64px; color: rgba(255,255,255,0.3); margin-bottom: 20px;">📭</div>
                                        <p style="color: rgba(255,255,255,0.6); font-size: 18px;">暂无历史记录</p>
                                    </div>
                                `;
                            }
                        }
                    });
                } else {
                    showToast(data.message || '删除失败', 'error');
                    gsap.to(btn.closest('.history-card'), {
                        scale: 1,
                        opacity: 1,
                        duration: 0.2,
                        ease: 'power2.out'
                    });
                }
            })
            .catch(error => {
                showToast('删除请求失败', 'error');
            });
        }

        function goToPage(pageNum) {
            window.location.href = contextPath + '/HistoryServlet?pageNum=' + pageNum;
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

        document.getElementById('image-modal').addEventListener('click', function(e) {
            if (e.target === this) {
                closeModal();
            }
        });

        document.addEventListener('keydown', function(e) {
            if (e.key === 'Escape') {
                closeModal();
            }
        });
    </script>
</body>
</html>