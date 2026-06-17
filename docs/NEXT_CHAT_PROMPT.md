# Next Chat Prompt

你正在接手 LifePilot 项目。

项目名称：LifePilot

项目定位：AI 个人生活管家平台，统一管理记账、购物清单、家庭库存、饮食计划、生活待办、票据文件、生活总结和 AI 分析。

技术栈：

- 后端：Java 17 target、Spring Boot 3、Spring Security、Validation、MyBatis-Plus、Flyway、MySQL、Maven。
- 前端：Vue 3、TypeScript、Vite、Element Plus、Pinia、Vue Router、Axios、ECharts。
- 基础设施：Docker Compose、MySQL、`.env.example`。
- AI：先使用 mock provider，后续再接 OpenAI-compatible provider。

当前阶段：Phase 2 用户与鉴权已完成 P0-005，下一步进入生活空间模型。

已完成内容：

- 文档体系。
- 后端 Spring Boot 骨架和健康检查。
- 前端 Vue 3 骨架和基础首页。
- Docker Compose、`.env.example`、`.gitignore`。
- 后端 `mvn test` 通过；前端 `npm run build` 通过；`npm audit --audit-level=high` 通过。
- 本地浏览器验证通过：首页显示 `lifepilot-backend UP`。
- 用户注册、登录、JWT 鉴权、当前用户接口和前端登录/注册页已实现。
- 真实 MySQL curl 和浏览器 UI 注册验证通过。
- 项目内 `agent-skills/` 已新增自主开发、后端模块、API/数据契约、AI mock provider、前端设计、本地 Web 验证、文档协作 skills。

未完成内容：

- 生活空间。
- 记账、购物、库存、统计和 AI mock provider。

最近一次修改：初始化项目文档和全栈骨架。

项目 skills：

- `agent-skills/lifepilot-auto-dev/SKILL.md`：继续自主开发、接手 backlog、更新交接状态。
- `agent-skills/lifepilot-backend-module/SKILL.md`：实现 Spring Boot 后端业务模块、权限和测试。
- `agent-skills/lifepilot-api-data-contract/SKILL.md`：设计或审查 API、数据库迁移、前端 API 类型契约。
- `agent-skills/lifepilot-ai-mock-provider/SKILL.md`：实现 AI mock provider、结构化草稿和用户确认流程。
- `agent-skills/lifepilot-frontend-design/SKILL.md`：设计或改造 Vue 3 前端页面。
- `agent-skills/lifepilot-webapp-testing/SKILL.md`：验证本地前端交互、浏览器 console、前后端联调。
- `agent-skills/lifepilot-doc-coauthoring/SKILL.md`：编写、重构或同步项目文档。

当前遗留问题：需要实现 P0-006 生活空间模型；Flyway 对 MySQL 8.4 输出建议升级警告但迁移成功。

下一步任务：阅读 `docs/BACKLOG.md`，执行 P0-006。

新 Agent 必须先阅读：

- `AGENTS.md`
- `docs/AUTO_DEV_PROTOCOL.md`
- `docs/CURRENT_STATE.md`
- `docs/BACKLOG.md`
- `docs/HANDOFF.md`
- `docs/CHANGELOG_AGENT.md`

任务匹配时，再阅读对应的 `agent-skills/*/SKILL.md`。

禁止事项：

- 不提交真实密钥或真实 `.env`。
- 不接入支付、短信、邮件、第三方登录、真实 OCR 付费服务。
- 不做医疗、法律、投资建议。
- 不自动购买或自动下单。
- 不用假数据长期冒充真实接口。

本地运行方式：

```bash
cp .env.example .env
docker compose up -d mysql
cd backend && mvn spring-boot:run
cd frontend && npm install && BACKEND_PROXY_TARGET=http://localhost:8080 npm run dev
```

若本机 `8080` 或 `3306` 被占用，可使用后端 `18081` 和 MySQL `3307`，详见 `docs/HANDOFF.md`。

测试方式：

```bash
cd backend && mvn test
cd frontend && npm install && npm run build
```

继续开发规则：除非触发 `docs/AUTO_DEV_PROTOCOL.md` 中的停止条件，否则完成一个任务后继续从 `docs/BACKLOG.md` 选择下一项最高优先级任务，不要停下来问用户下一步。
