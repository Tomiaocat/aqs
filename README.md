# HTTP加密通信工具

端到端AES加密通信方案，包含Java Swing桌面客户端和Spring Boot服务端。

## 技术规格

- **加密算法**: AES-128/CBC/PKCS5Padding
- **密钥派生**: MD5(输入字符串)取前16字节
- **IV处理**: 随机16字节，拼接在密文前，整体Base64编码
- **加密粒度**: JSON最外层value（嵌套对象整体加密）

## 项目结构

```
/aqs/
├── http-client/          # Java Swing GUI客户端
│   ├── pom.xml
│   └── src/main/java/com/httptool/
│       ├── Main.java                    # 程序入口
│       ├── crypto/AesUtil.java          # 加密工具（与服务端共用）
│       ├── gui/
│       │   ├── MainFrame.java           # 主窗口
│       │   ├── RequestPanel.java        # 请求配置面板
│       │   ├── ResponsePanel.java       # 响应展示面板
│       │   ├── HistoryPanel.java        # 历史记录面板
│       │   └── HeaderInputRow.java      # 请求头输入行
│       ├── http/HttpClient.java         # HTTP请求封装
│       ├── model/                       # 数据模型
│       ├── storage/                     # 数据存储
│       └── dialog/KeyManagerDialog.java # 密钥管理对话框
│
└── http-server/          # Spring Boot服务端
    ├── pom.xml
    └── src/main/java/com/httpserver/
        ├── HttpServerApplication.java     # 启动类
        ├── crypto/AesUtil.java            # 加密工具（与客户端共用）
        ├── interceptor/
        │   └── EncryptionInterceptor.java # 统一加解密拦截器
        ├── controller/DemoController.java # 示例接口
        └── model/ApiResponse.java         # 统一响应包装
```

## 快速开始

### 1. 启动服务端

```bash
cd http-server
mvn spring-boot:run
```

服务端将在端口8080启动。

### 2. 启动客户端

```bash
cd http-client
mvn exec:java
```

或打包后运行：

```bash
cd http-client
mvn package -DskipTests
java -jar target/http-client-1.0.0.jar
```

### 3. 测试通信

1. 在客户端输入服务端URL：`http://localhost:8080/api/user`
2. 输入AES密钥（默认：`your-secret-key-here`）
3. 输入JSON请求体：
   ```json
   {
     "id": 1,
     "name": "张三",
     "age": 25
   }
   ```
4. 点击"发送请求"
5. 查看响应面板中的明文结果

## 客户端功能

- **请求配置**: URL、HTTP方法、请求头、请求体
- **AES加密**: 自动加密请求体，解密响应体
- **响应展示**: 密文(Base64)、明文(JSON)、JSON树形视图
- **历史记录**: 自动保存请求历史，支持双击加载
- **模板管理**: 保存常用请求配置
- **密钥管理**: 管理多个AES密钥

## 服务端API

| 端点 | 方法 | 描述 |
|------|------|------|
| `/api/user` | POST | 创建用户（接收加密请求） |
| `/api/user/{id}` | GET | 获取用户（返回加密响应） |
| `/api/data` | POST | 通用数据处理 |
| `/api/health` | GET | 健康检查（不加密） |

## 加密通信流程

```
客户端(明文JSON) → AES加密(最外层value) → HTTP请求 → 服务端
                                                      ↓
客户端(展示明文) ← AES解密响应体 ← HTTP响应 ← 业务处理 ← 解密请求体
```

## 配置

### 服务端配置 (application.yml)

```yaml
api:
  encrypt:
    key: your-secret-key-here  # 通信密钥
    enabled: true              # 是否启用加密
```

### 客户端配置

密钥和模板存储在用户主目录：
- `~/.http-client/keys.json` - 密钥列表
- `~/.http-client/templates.json` - 请求模板
- `~/.http-client/history.json` - 请求历史

## 构建要求

- JDK 1.8 或更高版本
- Maven 3.6+

## 依赖

### 客户端
- Jackson 2.15.x (JSON处理)
- Apache HttpClient 4.5.x (HTTP请求)

### 服务端
- Spring Boot 2.7.x
- Jackson (JSON处理)
