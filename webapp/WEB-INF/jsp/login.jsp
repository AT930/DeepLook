<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>登录 - 深度可视化景深检测</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <script src="https://cdnjs.cloudflare.com/ajax/libs/gsap/3.12.5/gsap.min.js"></script>
</head>
<body>
    <div class="auth-container" id="auth-container">
        <div class="auth-card" id="auth-card">
            <h2 id="login-title">深度可视化景深检测</h2>
            
            <div id="message" class="message" style="display: none;">${requestScope.message}</div>
            
            <form id="login-form" action="${pageContext.request.contextPath}/LoginServlet" method="post">
                <div class="form-group">
                    <label for="username">用户名</label>
                    <input type="text" id="username" name="username" placeholder="请输入用户名" required>
                </div>
                
                <div class="form-group">
                    <label for="password">密码</label>
                    <input type="password" id="password" name="password" placeholder="请输入密码" required>
                </div>
                
                <button type="submit" class="auth-btn" id="login-btn">登录</button>
            </form>
            
            <div class="auth-link">
                还没有账号？<a href="${pageContext.request.contextPath}/RegisterServlet">立即注册</a>
            </div>
        </div>
    </div>

    <script>
        document.addEventListener('DOMContentLoaded', function() {
            gsap.from('#auth-card', {
                opacity: 0,
                y: 50,
                duration: 0.8,
                ease: 'power3.out',
                overwrite: true
            });

            gsap.from('#login-title', {
                opacity: 0,
                scale: 0.8,
                duration: 0.6,
                delay: 0.2,
                ease: 'back.out(1.7)',
                overwrite: true
            });

            gsap.from('.form-group', {
                opacity: 0,
                x: -30,
                duration: 0.5,
                stagger: 0.2,
                delay: 0.4,
                ease: 'power2.out',
                overwrite: true
            });

            gsap.from('#login-btn', {
                opacity: 0,
                scale: 0.9,
                duration: 0.5,
                delay: 0.8,
                ease: 'power2.out',
                overwrite: true,
                onComplete: function() {
                    document.getElementById('login-btn').style.opacity = '1';
                    document.getElementById('login-btn').style.transform = 'scale(1)';
                }
            });

            gsap.from('.auth-link', {
                opacity: 0,
                y: 10,
                duration: 0.5,
                delay: 1.0,
                ease: 'power2.out',
                overwrite: true
            });

            const loginForm = document.getElementById('login-form');
            const message = document.getElementById('message');

            loginForm.addEventListener('submit', function(e) {
                e.preventDefault();
                
                const username = document.getElementById('username').value;
                const password = document.getElementById('password').value;

                if (!username || !password) {
                    return;
                }

                gsap.to('#login-btn', {
                    scale: 0.95,
                    duration: 0.1,
                    yoyo: true,
                    repeat: 1
                });

                setTimeout(function() {
                    loginForm.submit();
                }, 200);
            });

            var msg = '${requestScope.message}';
            if (msg && msg != 'null') {
                message.textContent = msg;
                message.className = 'message error';
                message.style.display = 'block';
                gsap.from(message, {
                    opacity: 0,
                    y: -20,
                    duration: 0.3,
                    ease: 'power2.out'
                });
            }
        });
    </script>
</body>
</html>