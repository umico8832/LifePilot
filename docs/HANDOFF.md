# Handoff

## 项目基本信息

- 项目名称：LifePilot
- 项目定位：AI 个人生活管家平台
- 一句话目标：统一管理记账、购物清单、家庭库存、饮食计划、生活待办、票据文件和 AI 生活分析。

## 技术栈

- 后端：Java 17 target、Spring Boot 3、Spring Security、Validation、MyBatis-Plus、Flyway、MySQL、Maven。
- 前端：Vue 3、TypeScript、Vite、Element Plus、Pinia、Vue Router、Axios、ECharts。
- 基础设施：Docker Compose、MySQL、`.env.example`。
- AI：先使用 mock provider，后续再接 OpenAI-compatible provider。

## 当前阶段

Phase 2：用户与鉴权。用户注册、登录、JWT 鉴权和当前用户接口已完成。

## 已完成内容

- 创建 Agent 自主开发文档体系。
- 创建后端 Spring Boot 基础结构和健康检查。
- 创建前端 Vue 3 基础首页。
- 创建 Docker Compose、`.env.example`、`.gitignore`。
- 验证后端测试、前端构建和 high 级别 npm audit。
- 本地浏览器验证首页显示 `lifepilot-backend UP`，无 console error。
- 实现用户注册、登录、JWT 鉴权、当前用户接口和前端登录/注册页。
- 使用 MySQL Compose 在 `3307` 完成真实注册、登录、`/api/users/me` 验证。

## 运行方式

```bash
cp .env.example .env
docker compose up -d mysql

cd backend
mvn spring-boot:run

cd ../frontend
npm install
BACKEND_PROXY_TARGET=http://localhost:8080 npm run dev
```

本机若 `8080` 和 `3306` 被占用：

```bash
MYSQL_PORT=3307 docker compose up -d mysql

cd backend
BACKEND_PORT=18081 SPRING_DATASOURCE_URL='jdbc:mysql://localhost:3307/lifepilot?useUnicode=true&characterEncoding=utf8&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC' mvn spring-boot:run

cd ../frontend
BACKEND_PROXY_TARGET=http://localhost:18081 npm run dev
```

## 测试方式

```bash
cd backend && mvn test
cd frontend && npm install && npm run build
cd frontend && npm audit --audit-level=high
```

## 当前遗留问题

- 生活空间、记账、购物、库存等业务模块尚未实现。
- AI provider 尚未落代码，仅有设计方向。
- 前端生产构建提示单个 chunk 较大，后续需要结合路由分包优化。
- 如果 `8080` 被占用，可用 `BACKEND_PORT=18081 mvn spring-boot:run` 启后端，并用 `BACKEND_PROXY_TARGET=http://localhost:18081 npm run dev` 启前端。
- Flyway 对 MySQL 8.4 输出“建议升级”警告，但当前迁移成功。

## 下一步建议任务

P0-006 实现生活空间模型。

## 新对话续接提示词

如需新开对话，使用 `docs/NEXT_CHAT_PROMPT.md`。

## 接手注意事项

- 先读 `AGENTS.md` 和 `docs/AUTO_DEV_PROTOCOL.md`。
- 按 `docs/BACKLOG.md` 自动取下一项未完成任务。
- 不要接入真实密钥、支付、短信、邮件、第三方登录或真实 OCR 付费服务。
- 不要提供医疗、法律、投资建议。
