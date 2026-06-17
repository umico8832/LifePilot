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

Phase 5：购物清单。Phase 0-4 全部完成（文档、骨架、鉴权、空间、记账+分类）。

## 已完成内容

- Phase 0-1：文档体系、项目骨架、Spring Boot 配置、Vue 3 配置。
- Phase 2：用户注册登录、JWT 鉴权、当前用户接口。
- Phase 3：生活空间模型（Household、HouseholdMember、成员权限、注册自动创建个人空间）。
- Phase 4：支出记录 CRUD、收入记录 CRUD（共用 transaction_record）、消费分类管理（CategoryService/CategoryController）。
- 前端页面：AuthView、HomeView、SpaceView、FinanceView（含记账和分类 API）。
- 项目 Agent skills 体系。

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

- 购物清单、库存、待办等业务模块尚未实现。
- AI provider 尚未落代码，仅有设计方向。
- 前端分类管理 UI 尚未集成到 FinanceView。
- 前端生产构建提示单个 chunk 较大，后续需要结合路由分包优化。

## 下一步建议任务

P0-010 实现购物清单 CRUD。

## 接手注意事项

- 先读 `AGENTS.md` 和 `docs/AUTO_DEV_PROTOCOL.md`。
- 任务匹配时读取 `agent-skills/*/SKILL.md`。
- 按 `docs/BACKLOG.md` 自动取下一项未完成任务。
- 不要接入真实密钥、支付、短信、邮件、第三方登录或真实 OCR 付费服务。