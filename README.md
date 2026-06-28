# LifePilot

LifePilot 是一个 AI 个人生活管家平台，目标是把记账、购物清单、家庭库存、饮食计划、生活待办、票据文件、生活总结和 AI 分析统一管理起来。

当前仓库处于持续自主开发阶段：已实现账号、生活空间、记账、购物清单、库存、待办、菜谱、饮食计划、票据文档、统计面板和多项 AI mock 能力。

## Tech Stack

- Backend: Java 17 target, Spring Boot 3, Spring Security, Validation, MyBatis-Plus, Flyway, MySQL, Maven
- Frontend: Vue 3, TypeScript, Vite, Element Plus, Pinia, Vue Router, Axios, ECharts
- Infrastructure: Docker Compose, MySQL, `.env.example`
- AI: 先使用 mock provider 设计，后续再接 OpenAI-compatible provider

## Repository Layout

```text
backend/      Spring Boot API service
frontend/     Vue 3 web app
docs/         Agent-first project docs
```

## Local Setup

1. Copy environment variables:

```bash
cp .env.example .env
```

2. Start MySQL:

```bash
docker compose up -d mysql
```

3. Run backend:

```bash
cd backend
mvn spring-boot:run
```

4. Run frontend:

```bash
cd frontend
npm install
BACKEND_PROXY_TARGET=http://localhost:8080 npm run dev
```

Default URLs:

- Backend health check: `http://localhost:8080/api/health`
- Frontend dev server: `http://localhost:5173`

If port `8080` is already in use, run the backend on another port and point the frontend proxy at it:

```bash
cd backend
BACKEND_PORT=18081 mvn spring-boot:run

cd ../frontend
BACKEND_PROXY_TARGET=http://localhost:18081 npm run dev
```

## Verification

```bash
cd backend && mvn test
cd frontend && npm install && npm run build
```

## Demo Seed Data

本地演示数据库可使用可重复的 demo seed 脚本初始化。脚本只重置 `demo@lifepilot.local` 拥有的 demo 用户和空间数据，不会调用外部服务。

```bash
scripts/demo_seed.sh --dry-run
MYSQL_PORT=3307 scripts/demo_seed.sh --apply
MYSQL_PORT=3307 scripts/demo_seed.sh --verify
```

默认演示账号：

- Email: `demo@lifepilot.local`
- Password: `demo-pass-123`

演示数据覆盖首页统计图表、记账分类、购物预算、库存临期/低库存提醒、待办完成率、饮食计划、菜谱推荐素材、票据文档和 AI 日志页面。重复执行 `--apply` 会先清理该 demo 账号拥有的数据再重新插入。

## Agent Workflow

New Agents should read `AGENTS.md` first. Do not continue from memory alone. The autonomous development protocol, current state, backlog, handoff, and changelog are the source of truth for continuing work.
