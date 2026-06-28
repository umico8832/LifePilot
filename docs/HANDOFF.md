# Handoff

本文件是稳定接手手册，只记录长期有效的运行、测试和接手信息。

当前阶段、下一任务、阻塞项和最近验证结果以 `docs/CURRENT_STATE.md` 为准。任务池和任务状态以 `docs/BACKLOG.md` 为准。最近历史摘要以 `docs/RECENT_HISTORY.md` 为准。完整历史按需查看 `docs/CHANGELOG_AGENT.md` 和 `docs/changelog/`。

## 项目基本信息

- 项目名称：LifePilot
- 项目定位：AI 个人生活管家平台。
- 一句话目标：统一管理记账、购物清单、家庭库存、饮食计划、生活待办、票据文件和 AI 生活分析。

## 技术栈

- 后端：Java 17 target、Spring Boot 3、Spring Security、Validation、MyBatis-Plus、Flyway、MySQL、Maven。
- 前端：Vue 3、TypeScript、Vite、Element Plus、Pinia、Vue Router、Axios、ECharts。
- 基础设施：Docker Compose、MySQL、`.env.example`。
- AI：先使用 mock provider，后续再接 OpenAI-compatible provider。

## 接手流程

1. 读 `AGENTS.md`。
2. 读 `docs/AUTO_DEV_PROTOCOL.md`。
3. 读 `docs/CURRENT_STATE.md` 获取当前状态和下一任务。
4. 读 `docs/BACKLOG.md` 获取任务池和验收标准。
5. 读 `docs/RECENT_HISTORY.md` 了解最近历史。
6. 按任务类型读取对应设计文档和 `agent-skills/*/SKILL.md`。

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
cd backend && ./mvnw test
cd frontend && npm install && npm run build
cd frontend && npm audit --audit-level=high
python3 scripts/agent_changelog_archive.py
python3 scripts/agent_doc_check.py
```

如果 Maven wrapper 不可用，可临时使用：

```bash
cd backend && mvn test
```

## 常用入口

- 前端开发服务：`http://localhost:5173`
- 后端健康检查：`/api/health`
- Swagger UI：`/swagger-ui.html`
- OpenAPI JSON：`/v3/api-docs`

## 本地演示数据

项目提供 `scripts/demo_seed.sh` 和 `scripts/demo_seed.sql` 用于初始化可重复的本地演示数据。脚本默认 `--dry-run`，不会修改数据库；真实写入需显式使用 `--apply`。

```bash
scripts/demo_seed.sh --dry-run
MYSQL_PORT=3307 scripts/demo_seed.sh --apply
MYSQL_PORT=3307 scripts/demo_seed.sh --verify
```

默认演示账号：

- Email：`demo@lifepilot.local`
- Password：`demo-pass-123`

脚本只清理并重建该 demo 账号拥有的 demo 空间和业务数据，覆盖记账、购物清单、库存提醒、待办、菜谱、饮食计划、票据文档和 AI 调用日志。若本地使用默认 `3306` 端口，可省略 `MYSQL_PORT=3307`；也可通过 `MYSQL_HOST`、`MYSQL_DATABASE`、`MYSQL_USER`、`MYSQL_PASSWORD` 指向其他本地开发库。

## 安全边界

- 不提交真实 `.env`、真实密钥、数据库密码或外部服务凭据。
- 不接入支付、短信、邮件、第三方登录或真实 OCR 付费服务，除非用户明确作出新阶段决策。
- AI provider 默认 mock；真实 provider 只能通过环境变量配置，不把 API Key 写入代码。
- 不做医疗诊断、药品剂量、投资建议、法律判断、自动购买或自动下单。

## 交接注意事项

- `docs/HANDOFF.md` 不记录当前阶段和下一任务，避免和 `docs/CURRENT_STATE.md` 漂移。
- `docs/NEXT_CHAT_PROMPT.md` 只保留极简入口，避免复制状态。
- 完成任务后至少更新 `docs/CURRENT_STATE.md`、`docs/BACKLOG.md`、`docs/CHANGELOG_AGENT.md`，运行 `python3 scripts/agent_changelog_archive.py` 刷新近期历史，再运行 `python3 scripts/agent_doc_check.py`。
