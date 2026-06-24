# DeepLook - 深度可视化景深检测系统

一个基于 Depth Anything V2 的深度可视化景深检测网页系统，支持 GPU 加速处理。

## ✨ 功能特性

- 🔐 **用户认证系统** - 登录/注册，会话管理
- 📤 **图片上传** - 支持 JPG/PNG/TIFF/RAW/WebP 格式，拖拽上传
- 🤖 **深度估计** - 集成 Depth Anything V2 模型，GPU 加速
- 🎨 **可视化效果** - 前景(红)、合焦(绿)、远景(蓝) 三档分层渲染
- 📊 **实时调节** - 阈值和透明度参数实时调整
- 💾 **图片下载** - 支持下载原图、深度图、可视化效果图
- 📜 **历史记录** - 查看分析历史

## 🏗️ 技术栈

### 后端
- **Java 17+** - 编程语言
- **Jakarta EE 10** - Web 框架
- **Servlet + JSP** - MVC 架构
- **MySQL 8.0** - 数据库
- **Maven** - 项目构建
- **Tomcat 11** - Web 服务器

### 前端
- **HTML5 / CSS3 / JavaScript** - 基础技术
- **GSAP** - 动画效果
- **Canvas API** - 深度图可视化渲染

### 深度估计
- **Depth Anything V2** - ViT-Base 模型
- **PyTorch + MPS** - Apple Silicon GPU 加速
- **Python Flask** - API 服务封装

## 📁 项目结构

```
DeepLook/
├── src/main/java/com/depth/
│   ├── bean/           # 实体类 (User, ImageFile, DepthRecord)
│   ├── controller/     # 控制器 (Servlet)
│   ├── dao/            # 数据访问层
│   ├── filter/         # 过滤器 (EncodingFilter, LoginFilter)
│   ├── service/        # 业务逻辑层
│   └── util/           # 工具类
├── src/main/resources/
│   └── db.properties   # 数据库配置 (示例: db.properties.example)
├── webapp/
│   ├── WEB-INF/
│   │   ├── jsp/        # JSP 页面
│   │   └── web.xml     # Web 配置
│   ├── css/            # 样式文件
│   ├── js/             # JavaScript 文件
│   └── depthOutput/    # 深度图输出目录
├── pom.xml             # Maven 配置
└── .gitignore          # Git 忽略文件
```

## 🚀 快速开始

### 环境要求

- JDK 17+
- Maven 3.8+
- MySQL 8.0+
- Python 3.10+
- PyTorch 2.x
- Tomcat 11

### 1. 数据库配置

```sql
CREATE DATABASE depth_visual CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE depth_visual;

CREATE TABLE tb_user (
    uid INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(100) NOT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE tb_image_file (
    id INT PRIMARY KEY AUTO_INCREMENT,
    uid INT NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    file_size BIGINT,
    file_type VARCHAR(20),
    upload_time DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE tb_depth_record (
    id INT PRIMARY KEY AUTO_INCREMENT,
    image_id INT NOT NULL,
    depth_image_path VARCHAR(500),
    threshold INT DEFAULT 128,
    opacity DOUBLE DEFAULT 0.7,
    analyze_time DATETIME DEFAULT CURRENT_TIMESTAMP
);
```

### 2. 配置数据库连接

复制 `src/main/resources/db.properties.example` 为 `db.properties` 并修改配置：

```properties
jdbc.driver=com.mysql.cj.jdbc.Driver
jdbc.url=jdbc:mysql://localhost:3306/depth_visual?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
jdbc.username=root
jdbc.password=your_password
```

### 3. 启动深度估计服务 (GPU加速)

```bash
cd /tmp
python3 depth_server_gpu.py
```

服务将在 `http://localhost:5001` 启动。

### 4. 编译部署

```bash
# 编译项目
mvn clean package -DskipTests

# 部署到 Tomcat
cp target/depth-visual.war $TOMCAT_HOME/webapps/

# 启动 Tomcat
catalina start
```

### 5. 访问系统

打开浏览器访问：http://localhost:8080/depth-visual/

默认账号：
- 用户名：`admin`
- 密码：`admin123`

## 📡 API 接口

### 深度估计服务 (端口 5001)

| 接口 | 方法 | 说明 |
|------|------|------|
| `/` | GET | 服务信息 |
| `/health` | GET | 健康检查 |
| `/predict` | POST | 返回深度图 PNG |
| `/predict_data` | POST | 返回深度数据 JSON |

## 🎯 可视化效果

| 区域 | 颜色 | 深度值范围 |
|------|------|-----------|
| 前景 (近) | 🔴 红色 | 0 - 60 |
| 合焦 (中) | 🟢 绿色 | 61 - 180 |
| 远景 (远) | 🔵 蓝色 | 181 - 255 |

## ⚡ 性能

- **GPU**: Apple M2 Pro (19 GPU Cores)
- **加速方式**: MPS (Metal Performance Shaders)
- **处理速度**: ~0.12秒/张 (518x518 输入)

## 📝 License

MIT License
