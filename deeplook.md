深度可视化景深检测网页系统设计文档（JavaWeb MVC）
一、项目概述
1.1 项目简介
本项目为图片景深可视化分析网页工具，对标 Lightroom 景深蒙版可视化功能，上传图片后自动区分画面景深区域，使用差异化色彩标记前景、焦平面、背景虚化区域，直观展示画面景深分布。整体基于 JavaWeb 标准 MVC 架构开发，包含用户注册登录、图片上传存储、图片景深 AI / 算法解析、景深色彩可视化渲染、历史图片记录管理全套功能。
前端页面交互顺滑，支持图片拖拽上传、实时预览、景深色彩阈值调节、可视化图下载；后端使用 Servlet、Filter、DAO、JavaBean 分层解耦，MySQL 存储用户、图片、分析记录数据。
1.2 技术栈
后端：Java8、Servlet、Filter、JDBC、DAO 分层、JavaBean、MVC 架构、图片 IO 处理、第三方景深解析 API
数据库：MySQL8.0
前端：HTML、JSP、CSS、JS、AJAX、Canvas（景深色彩渲染）、EL&JSTL
配套工具：文件上传工具、图片压缩工具、分页工具、编码过滤器、登录拦截过滤器
核心特色：景深分层色彩可视化（复刻 LR 景深蒙版），近景红色、合焦区绿色、远景蓝色分层标注
1.3 核心功能
用户模块：账号注册、登录、未登录访问拦截、会话权限控制
图片上传：本地拖拽 / 点击上传、图片格式校验（jpg/png/webp）、文件大小压缩
景深可视化分析：
解析图片深度信息，划分三层景深区间
色彩蒙版叠加原图：前景浅红、清晰焦区绿色、远景浅蓝
支持调节景深阈值、蒙版透明度
图片管理：历史上传图片分页列表、原图 / 景深可视化图预览、删除记录、下载处理后图片
全局组件：全站 UTF-8 编码过滤、登录权限拦截、文件类型安全校验
二、整体 MVC 分层架构
自上而下分层：
View视图层 → Controller(Servlet)控制层 → Service业务层 → DAO数据访问层 → MySQL数据库
附加组件：Filter 过滤器、JavaBean 实体、通用工具类、配置文件
2.1 各分层职责
1. View 视图层
作用：页面展示、接收图片上传 / 参数调节、渲染原图与景深可视化画布，无业务逻辑
技术：JSP、HTML、Canvas、JS、AJAX、EL/JSTL
存放目录：webapp/
页面包含：登录页、注册页、首页上传面板、图片景深分析页、历史记录页、错误提示页
静态资源：css、js、上传图片存储目录、预览素材
2. Controller 控制层（Servlet）
包路径：com.depth.controller
职责：
接收表单 / 上传文件 / AJAX 参数
调用对应 Service 业务方法
文件上传、图片流转处理
将原图、景深分析结果存入 request/session 域
请求转发 / 重定向页面跳转
3. Service 业务逻辑层
包路径：com.depth.service + com.depth.service.impl（接口 + 实现）
职责：
用户注册登录逻辑、密码校验
图片上传校验、文件重命名、本地存储逻辑
景深分析核心业务：调用深度解析接口、生成分层色彩蒙版
数据分页、多表业务组合、异常捕获
4. DAO 数据访问层
包路径：com.depth.dao + com.depth.dao.impl
职责：仅操作 MySQL，完成用户、图片记录增删改查，无业务逻辑，封装 JDBC 查询更新
5. Model 模型层（JavaBean）
包路径：com.depth.bean
每张数据表对应一个实体类，私有属性 + get/set + 无参构造，承载数据库数据
6. Filter 全局过滤器
包路径：com.depth.filter
EncodingFilter：全局统一 UTF-8 编码，解决上传、页面乱码
LoginFilter：拦截分析页、上传页、历史页，未登录强制跳转登录页
7. 工具类 util
包路径：com.depth.util
JDBCUtil：数据库连接获取与关闭
FileUploadUtil：图片上传、格式校验、压缩、存储
DepthVisualUtil：景深分层色彩渲染工具
PageUtil：分页封装工具
8. 数据库层 MySQL
三张核心数据表：用户表、图片资源表、景深分析参数记录表
三、完整项目包目录结构
plaintext
src/main/java
└── com
    └── depth
        ├── bean                    // Model实体层
        │   ├── User.java           // 用户实体
        │   ├── ImageFile.java      // 上传图片实体
        │   └── DepthRecord.java    // 景深分析记录实体
        ├── dao                     // DAO接口
        │   ├── UserDao.java
        │   ├── ImageFileDao.java
        │   └── DepthRecordDao.java
        ├── dao/impl                // DAO实现类
        │   ├── UserDaoImpl.java
        │   ├── ImageFileDaoImpl.java
        │   └── DepthRecordDaoImpl.java
        ├── service                 // 业务接口
        │   ├── UserService.java
        │   ├── ImageService.java
        │   └── DepthService.java
        ├── service/impl            // 业务实现类
        │   ├── UserServiceImpl.java
        │   ├── ImageServiceImpl.java
        │   └── DepthServiceImpl.java
        ├── controller              // Servlet控制器
        │   ├── LoginServlet.java
        │   ├── RegisterServlet.java
        │   ├── UploadServlet.java
        │   ├── DepthAnalyzeServlet.java
        │   └── HistoryServlet.java
        ├── filter                  // 过滤器
        │   ├── EncodingFilter.java
        │   └── LoginFilter.java
        └── util                    // 工具类
            ├── JDBCUtil.java
            ├── FileUploadUtil.java
            ├── DepthVisualUtil.java
            └── PageUtil.java

src/main/resources
└── db.properties          // 数据库连接配置

webapp
├── WEB-INF
│   ├── web.xml            // Servlet、Filter注册配置
│   └── jsp
│       ├── login.jsp      // 登录页面
│       ├── register.jsp   // 注册页面
│       ├── index.jsp      // 图片上传首页
│       ├── analyze.jsp    // 景深可视化分析页面（Canvas渲染）
│       ├── history.jsp    // 历史图片与景深记录
│       └── error.jsp      // 异常错误页
├── css
│   └── style.css          // 页面样式、Canvas蒙版样式
├── js
│   ├── upload.js          // 拖拽上传、AJAX提交
│   ├── depthCanvas.js     // 景深色彩可视化渲染脚本
│   └── common.js
├── uploadImg              // 服务器存储上传原图目录
└── depthOutput            // 存储景深色彩可视化成品图
四、MySQL 数据库表设计
4.1 用户表 tb_user
sql
CREATE TABLE tb_user (
    uid INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(30) NOT NULL UNIQUE COMMENT '登录账号',
    password VARCHAR(50) NOT NULL COMMENT '登录密码',
    create_time DATETIME DEFAULT NOW() COMMENT '注册时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
4.2 上传图片表 tb_image_file
sql
CREATE TABLE tb_image_file (
    img_id INT PRIMARY KEY AUTO_INCREMENT,
    uid INT NOT NULL COMMENT '上传用户ID',
    origin_name VARCHAR(100) NOT NULL COMMENT '原始文件名',
    save_name VARCHAR(100) NOT NULL COMMENT '服务器存储文件名',
    file_path VARCHAR(200) NOT NULL COMMENT '原图存储路径',
    file_size INT COMMENT '文件大小kb',
    upload_time DATETIME DEFAULT NOW(),
    FOREIGN KEY (uid) REFERENCES tb_user(uid) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT '用户上传原图表';
4.3 景深分析记录表 tb_depth_record
sql
CREATE TABLE tb_depth_record (
    record_id INT PRIMARY KEY AUTO_INCREMENT,
    img_id INT NOT NULL COMMENT '关联原图ID',
    depth_output_path VARCHAR(200) NOT NULL COMMENT '景深可视化成品图路径',
    near_threshold INT DEFAULT 60 COMMENT '前景景深阈值',
    far_threshold INT DEFAULT 180 COMMENT '远景景深阈值',
    mask_opacity DECIMAL(2,1) DEFAULT 0.4 COMMENT '蒙版透明度',
    analyze_time DATETIME DEFAULT NOW(),
    FOREIGN KEY (img_id) REFERENCES tb_image_file(img_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT '景深可视化分析参数记录';
五、景深可视化核心逻辑说明
复刻 Lightroom 景深蒙版逻辑，通过深度灰度图分层映射彩色蒙版：
图片解析生成深度灰度图：像素灰度值代表景深远近（0 = 极近，255 = 极远）
分层色彩映射规则：
灰度 0~ 近阈值：前景区域 → 叠加红色半透明蒙版
灰度 近阈值～远阈值：合焦清晰区域 → 叠加绿色半透明蒙版
灰度 远阈值～255：远景虚化区域 → 叠加蓝色半透明蒙版
前端 Canvas 叠加原图与彩色蒙版，支持实时拖动滑块调整阈值、蒙版透明度
后端可导出合并后的景深可视化成品图片保存至服务器
六、完整请求流程（上传图片 + 景深分析示例）
View 层：用户在 index.jsp 拖拽 / 点击上传图片，JS 校验格式大小，AJAX 提交至 UploadServlet
Filter 拦截：EncodingFilter 统一编码；LoginFilter 校验登录状态，未登录跳转 login.jsp
Controller(UploadServlet)：接收 Multipart 文件，调用 FileUploadUtil 存储图片，调用 ImageService 保存图片记录
Service(ImageServiceImpl)：校验文件安全、生成唯一存储文件名，调用 ImageFileDao 写入数据库
DAO(ImageFileDaoImpl)：执行 INSERT SQL，保存图片信息，返回图片主键 img_id
页面转发至 analyze.jsp，携带原图路径与 img_id
View 层 Canvas：页面加载原图，发送 AJAX 请求 DepthAnalyzeServlet
Controller(DepthAnalyzeServlet)：接收 img_id、景深阈值、透明度参数，调用 DepthService
Service(DepthServiceImpl)：读取原图，调用 DepthVisualUtil 生成深度分层色彩蒙版，合成可视化图片，调用 DepthRecordDao 保存分析参数记录
处理完成后将成品图路径、分层数据返回前端
View 层：JS 渲染 Canvas，分层展示红 / 绿 / 蓝景深蒙版，支持实时调节参数刷新可视化效果，支持下载成品图
七、各组件对应汇总表
表格
分层	技术组件	包 / 目录位置	核心作用
View	JSP、HTML、Canvas、JS	webapp/jsp、webapp/js、css	上传、景深色彩可视化预览、参数调节
Controller	Servlet	com.depth.controller	文件接收、请求分发、页面跳转
Service	Service 接口 + 实现类	com.depth.service	图片上传业务、景深分层渲染业务、参数校验
DAO	DAO 接口 + 实现类	com.depth.dao	用户、图片、景深记录 CRUD
Model	JavaBean 实体	com.depth.bean	封装用户、图片、分析记录数据
Filter	EncodingFilter、LoginFilter	com.depth.filter	编码统一、登录权限拦截
工具类	各类通用工具	com.depth.util	JDBC、文件上传、景深色彩渲染、分页
八、项目开发顺序
创建项目目录结构，编写 web.xml 配置 Servlet 与 Filter
编写 db.properties 与 JDBCUtil 数据库工具类
执行 MySQL 建表 SQL，初始化三张数据表
开发所有 JavaBean 实体类（User、ImageFile、DepthRecord）
开发 DAO 接口与 DAO 实现类，完成基础增删改查
开发 Service 接口与业务实现类
编写工具类：文件上传工具、景深色彩可视化渲染工具、分页工具
开发两个 Filter 过滤器（编码、登录拦截）
开发全部 Servlet 控制器（登录、注册、上传、景深分析、历史记录）
开发前端页面：登录、注册、上传首页、景深 Canvas 分析页、历史记录页，编写上传与景深渲染 JS 脚本
前后端联调、图片上传测试、景深色彩可视化效果调试
异常处理、分页优化、图片压缩、安全校验完善
九、系统安全约束
上传文件校验后缀、MIME 类型，禁止脚本文件上传
图片自动重命名，防止路径穿越、文件名覆盖
登录拦截器保护核心页面，未登录无法上传与分析图片
数据库密码、文件存储路径统一配置在 properties，不硬编码
限制单张图片上传大小，自动压缩大图减轻服务器压力
用户数据隔离，仅可查看自己上传的图片与景深分析记录
十、系统核心优势
复刻 Lightroom 专业景深可视化，三色分层标记前景 / 合焦 / 远景，直观查看虚化分布
标准 JavaWeb MVC 分层，Servlet+DAO+Filter 传统教学技术栈，适配课程设计 / 毕设
完整用户体系：注册登录、会话拦截、个人图片历史记录
前端 Canvas 实时交互，动态调节景深阈值与蒙版透明度，所见即所得
图片持久化存储，所有景深分析参数自动存档，支持二次查看、重新渲染
工具类高度封装，图片上传、景深色彩渲染逻辑解耦，易于拓展新功能（导出原图蒙版、批量分析等）


生成深度可视化景深检测网页系统的后端代码

