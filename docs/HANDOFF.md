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

Phase 12 完成 + P1-002 完成。Phase 0-6, 9-10, 12, P1-001, P1-002 全部完成（文档、骨架、鉴权、空间、记账+分类、购物清单、库存、AI mock provider、首页统计面板、统一异常处理、GitHub Actions CI）。

## 已完成内容

- Phase 0-1：文档体系、项目骨架、Spring Boot 配置、Vue 3 配置。
- Phase 2：用户注册登录、JWT 鉴权、当前用户接口。
- Phase 3：生活空间模型（Household、HouseholdMember、成员权限、注册自动创建个人空间）。
- Phase 4：支出记录 CRUD、收入记录 CRUD（共用 transaction_record）、消费分类管理（CategoryService/CategoryController）。
- Phase 5：购物清单和清单项 CRUD（ShoppingList/ShoppingItem/ShoppingService/ShoppingController）。
- Phase 6：库存物品 CRUD 和低库存提醒（InventoryItem/InventoryService/InventoryController）。
- Phase 9：AI mock provider 自然语言记账（AiProvider/MockAiProvider/AiService/AiController）。
- Phase 10：首页统计面板（StatisticService/StatisticController，overview + finance/monthly 两个端点）。
- P1-001：完善错误码和异常处理（GlobalExceptionHandler 统一处理 8 类异常，所有错误响应使用 ApiResponse.error 结构）。
- P1-002：GitHub Actions CI（`.github/workflows/ci.yml`，后端 Maven test + 前端 npm build 并行 jobs）；Maven wrapper（`backend/mvnw`）。
- 前端页面：AuthView、HomeView（统计仪表盘）、SpaceView、FinanceView（含 AI 记账）、ShoppingView、InventoryView。
- 项目 Agent skills 体系（7 个 skills）。

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

- 待办模块尚未实现。
- AI 其他端点（create-shopping-list-draft、create-todo-draft、monthly-report-draft）尚未实现。
- 前端构建提示单个 chunk 较大，后续需要结合路由分包优化。
- 前端分类管理 UI 尚未集成到 FinanceView。
- CI 需 push 到 GitHub 后才能在 GitHub Actions 面板验证。

## 下一步建议任务

P1-003 增加 OpenAPI 文档。

## 接手注意事项

- 先读 `AGENTS.md` 和 `docs/AUTO_DEV_PROTOCOL.md`。
- 任务匹配时读取 `agent-skills/*/SKILL.md`。
- 按 `docs/BACKLOG.md` 自动取下一项未完成任务。
- 不要接入真实密钥、支付、短信、邮件、第三方登录或真实 OCR 付费服务。
- 本机 `8080` 和 `3306` 端口被占用，当前开发服务使用后端 `18081`、MySQL `3307` 和前端 `5173`。