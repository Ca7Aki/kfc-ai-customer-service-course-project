# KFC 智能售后客服系统

基于 **Spring Boot + LangChain4j + RAG** 的 AI 智能客服项目，包含后端服务与 uni-app 微信小程序前端。

## 项目简介

- **后端 `mychatting/`**：Spring Boot 3.2 + LangChain4j，接入 DeepSeek 聊天模型（普通/流式）与阿里云百炼 Qwen 向量嵌入模型，基于 KFC 售后知识库（RAG）实现检索增强问答，支持多会话记忆（文件持久化）。
- **前端 `KFCWeb/`**：uni-app（Vue3）微信小程序，KFC 红配色的聊天界面，调用后端 `/ai/chat-json` 接口。

## 技术栈

| 端 | 技术 |
| --- | --- |
| 后端 | Java 17、Spring Boot 3.2.5、LangChain4j 1.0.0-beta3、Maven |
| 前端 | uni-app、Vue 3、微信小程序 |

## 目录结构

```
.
├── mychatting/                # 后端（Spring Boot）
│   ├── src/main/java/         # Java 源码
│   │   └── org/example/mychatting/
│   │       ├── config/        # AiConfig（模型/RAG/记忆配置）、FileChatMemoryStore
│   │       ├── controller/    # ChatController（/ai/chat 流式、/ai/chat-json）
│   │       └── service/       # ToolsService（Function Calling 工具示例）
│   └── src/main/resources/
│       ├── application.properties  # 服务与模型配置（密钥走环境变量）
│       ├── rag/rag-service.txt     # RAG 知识库：KFC 售后规则
│       └── static/index.html       # 内置网页测试页面
└── KFCWeb/                    # 前端（uni-app 小程序）
    ├── pages/index/index.vue  # 聊天主页面
    ├── manifest.json          # uni-app 配置
    └── ...
```

## 快速开始

### 1. 后端

```bash
cd mychatting
mvn spring-boot:run
```

启动前需配置两个环境变量（见下文），服务默认端口 `9000`。

接口说明：

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/ai/chat?memoryId=xx&message=xx` | 流式对话（SSE），网页端使用 |
| POST | `/ai/chat-json` | 普通 JSON 对话，小程序使用，请求体 `{"message":"...","memoryId":"default"}` |

后端内置网页测试页：启动后访问 <http://localhost:9000/index.html>。

### 2. 前端（微信小程序）

用 **HBuilderX** 打开 `KFCWeb/` 目录，运行到微信小程序即可。

> 注意：`pages/index/index.vue` 中的 `BASE_URL` 默认为 `http://localhost:9000`，真机调试时请改为你电脑的局域网 IP；微信开发者工具需勾选「不校验合法域名」。

## API 密钥配置（重要）

本项目**不包含任何真实密钥**，所有密钥均通过环境变量注入，请自行申请并配置：

| 环境变量 | 用途 | 申请地址 |
| --- | --- | --- |
| `DEEPSEEK_API_KEY` | DeepSeek 聊天模型（普通 + 流式） | https://platform.deepseek.com/ |
| `DASHSCOPE_API_KEY` | 阿里云百炼 Qwen 向量嵌入（RAG） | https://bailian.console.aliyun.com/ |

配置方式（任选其一）：

1. **IDEA**：Run/Debug Configurations → Environment variables 中添加；
2. **命令行启动**：
   ```bash
   # Windows PowerShell
   $env:DEEPSEEK_API_KEY="sk-xxx"; $env:DASHSCOPE_API_KEY="sk-xxx"; mvn spring-boot:run
   # Linux / macOS
   export DEEPSEEK_API_KEY="sk-xxx"
   export DASHSCOPE_API_KEY="sk-xxx"
   mvn spring-boot:run
   ```
3. **本地私密配置**：创建 `src/main/resources/application-local.properties`（已被 .gitignore 忽略，不会提交）覆盖同名配置项。

未配置密钥时应用启动会报「Could not resolve placeholder」错误，属预期行为。

## 其他说明

- `mychatting/chat_memories/` 为运行时生成的聊天记忆数据，已加入 .gitignore，首次运行自动创建。
- `target/`、`unpackage/`、`.idea/` 等构建产物与 IDE 配置均不纳入仓库。
