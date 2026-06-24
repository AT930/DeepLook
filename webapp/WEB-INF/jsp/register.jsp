<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>注册 - 深度可视化景深检测</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <script src="https://cdnjs.cloudflare.com/ajax/libs/gsap/3.12.5/gsap.min.js"></script>
</head>
<body>
    <div class="auth-container">
        <div class="auth-card" id="register-card">
            <h2 id="register-title">创建账号</h2>
            
            <div id="message" class="message" style="display: none;"></div>
            
            <form id="register-form">
                <div class="form-group">
                    <label for="username">用户名</label>
                    <input type="text" id="username" name="username" placeholder="请输入用户名" required>
                </div>
                
                <div class="form-group">
                    <label for="password">密码</label>
                    <input type="password" id="password" name="password" placeholder="请输入密码" required>
                </div>
                
                <div class="form-group">
                    <label for="confirmPassword">确认密码</label>
                    <input type="password" id="confirmPassword" name="confirmPassword" placeholder="请再次输入密码" required>
                </div>
                
                <button type="submit" class="auth-btn">注册</button>
            </form>
            
            <div class="auth-link">
                已有账号？<a href="${pageContext.request.contextPath}/LoginServlet">立即登录</a>
            </div>
        </div>
    </div>

    <script>
        var contextPath = '${pageContext.request.contextPath}';
        document.addEventListener('DOMContentLoaded', function() {
            gsap.from('#register-card', {
                opacity: 0,
                y: 50,
                duration: 0.8,
                ease: 'power3.out'
            });

            gsap.from('#register-title', {
                opacity: 0,
                scale: 0.8,
                duration: 0.6,
                delay: 0.2,
                ease: 'back.out(1.7)'
            });

            gsap.from('.form-group', {
                opacity: 0,
                x: -30,
                duration: 0.5,
                stagger: 0.15,
                delay: 0.4,
                ease: 'power2.out'
            });

            gsap.from('.auth-btn', {
                opacity: 0,
                scale: 0.9,
                duration: 0.5,
                delay: 0.9,
                ease: 'power2.out'
            });

            gsap.from('.auth-link', {
                opacity: 0,
                y: 10,
                duration: 0.5,
                delay: 1.1,
                ease: 'power2.out'
            });

            const registerForm = document.getElementById('register-form');
            const message = document.getElementById('message');

            registerForm.addEventListener('submit', function(e) {
                e.preventDefault();
                
                const username = document.getElementById('username').value;
                const password = document.getElementById('password').value;
                const confirmPassword = document.getElementById('confirmPassword').value;

                if (password !== confirmPassword) {
                    message.className = 'message error';
                    message.textContent = '两次输入的密码不一致';
                    message.style.display = 'block';
                    gsap.from(message, {
                        opacity: 0,
                        y: -20,
                        duration: 0.3,
                        ease: 'power2.out'
                    });
                    return;
                }

                if (password.length < 6) {
                    message.className = 'message error';
                    message.textContent = '密码长度不能少于6位';
                    message.style.display = 'block';
                    gsap.from(message, {
                        opacity: 0,
                        y: -20,
                        duration: 0.3,
                        ease: 'power2.out'
                    });
                    return;
                }

                gsap.to('.auth-btn', {
                    scale: 0.95,
                    duration: 0.1,
                    yoyo: true,
                    repeat: 1
                });

                fetch(contextPath + '/RegisterServlet', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/x-www-form-urlencoded'
                    },
                    body: 'username=' + encodeURIComponent(username) + '&password=' + encodeURIComponent(password)
                })
                .then(response => response.json())
                .then(data => {
                    if (data.success) {
                        message.className = 'message success';
                        message.textContent = data.message;
                        message.style.display = 'block';
                        
                        gsap.to('#register-card', {
                            opacity: 0,
                            y: -50,
                            duration: 0.5,
                            ease: 'power3.in',
                            onComplete: function() {
                                window.location.href = contextPath + '/LoginServlet';
                            }
                        });
                    } else {
                        message.className = 'message error';
                        message.textContent = data.message;
                        message.style.display = 'block';
                        
                        gsap.from(message, {
                            opacity: 0,
                            y: -20,
                            duration: 0.3,
                            ease: 'power2.out'
                        });
                    }
                })
                .catch(error => {
                    message.className = 'message error';
                    message.textContent = '注册失败，请重试';
                    message.style.display = 'block';
                });
            });
        });
    </script>
</body>
</html>