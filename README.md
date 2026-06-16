# LifePilot

LifePilot 是一个 AI 个人生活管家平台，目标是把记账、购物清单、家庭库存、饮食计划、生活待办、票据文件、生活总结和 AI 分析统一管理起来。

当前仓库处于早期骨架阶段：已建立项目文档体系、后端 Spring Boot 基础服务、前端 Vue 3 基础首页、Docker Compose 和环境变量示例。

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

## Agent Workflow

New Agents should read `AGENTS.md` first. Do not continue from memory alone. The autonomous development protocol, current state, backlog, handoff, and changelog are the source of truth for continuing work.
