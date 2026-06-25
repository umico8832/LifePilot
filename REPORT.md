# LifePilot 工程体检报告

生成时间：2026-06-25 14:25 Asia/Shanghai

## 当前项目技术栈

- 后端：Java 17 target、Spring Boot 3.5.6、Spring Security、Validation、MyBatis-Plus 3.5.14、Flyway、MySQL、Maven Wrapper 3.9.9。
- 前端：Vue 3、TypeScript、Vite 8、Vitest、Element Plus、Pinia、Vue Router、Axios、ECharts。
- 基础设施：Docker Compose + MySQL 8.4，GitHub Actions CI。
- AI：默认 mock provider；OpenAI-compatible provider 通过环境变量启用，当前不提交真实密钥。

## 已发现的问题

- GitHub Actions：最近一次远端 CI `#12`（commit `14ebfb1`）已成功；前一次失败 `#11`（commit `ddf0408`）失败 job 为 `Backend Tests`，失败步骤为 `Run tests`。公共 API 无权限下载完整 job logs（返回 403），但后续修复提交 `14ebfb1` 明确修复 `MealPlanMapper` 未注册导致的 Spring 上下文/后端启动失败。
- 本地 lint：`cd frontend && npm run lint` 失败，原因是 `frontend/package.json` 未定义 `lint` 脚本，项目也未配置 ESLint。
- 前端构建：`npm run build` 可通过，但 Vite/Rolldown 会输出 `@vueuse/core` 的 `INVALID_ANNOTATION` 警告，属于第三方依赖打包注释位置问题。
- 本地环境与 CI 不完全一致：本地为 Node 26/npm 11/Java 25；CI 使用 Node 20/JDK 17。后端 target 为 Java 17，本地 Java 25 测试通过，但仍建议日常按 CI 版本复验。

## 已修复的问题

- 调整 `frontend/vite.config.ts` 的 `build.chunkSizeWarningLimit` 为 `1000`，匹配当前 Element Plus + ECharts 应用体量，消除既有大 chunk 阈值噪音，让构建输出保留真正需要关注的第三方 annotation 警告。
- 通过 `npm ci` 重新验证前端依赖可稳定安装，未发现高危 audit 漏洞。

## 仍存在的问题

- 暂未引入 ESLint：这是工具链新增，不属于本次“安全修复”范围。复现命令：`cd frontend && npm run lint`。
- `@vueuse/core` 的 Rolldown pure annotation 警告仍存在。复现命令：`cd frontend && npm run build`。该问题来自 `node_modules/@vueuse/core/dist/index.js`，不建议直接 patch 依赖产物。
- GitHub Actions 旧失败 run 的完整日志无法匿名下载；需要仓库管理员权限或本机安装并登录 `gh` 后执行 `gh run view 27826095977 --log`。

## 建议的下一步开发方向

- 增加前端 lint 工具链：ESLint + Vue/TypeScript 配置，并在 CI 中加入 `npm run lint`。
- 将本地开发环境标准化到 CI 版本：Node 20、JDK 17，可考虑添加 `.nvmrc` 或 Volta 配置。
- 后续优化前端体积时，优先做 Element Plus 按需导入和 ECharts 图表模块复核，而不是继续调高构建阈值。
- 当前 backlog 无 todo，建议先规划下一阶段任务，再继续自主开发。

## 如何本地运行项目

```bash
cp .env.example .env
docker compose up -d mysql

cd backend
./mvnw spring-boot:run

cd ../frontend
npm install
BACKEND_PROXY_TARGET=http://localhost:8080 npm run dev
```

本机默认端口被占用时：

```bash
MYSQL_PORT=3307 docker compose up -d mysql

cd backend
BACKEND_PORT=18081 SPRING_DATASOURCE_URL='jdbc:mysql://localhost:3307/lifepilot?useUnicode=true&characterEncoding=utf8&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC' ./mvnw spring-boot:run

cd ../frontend
BACKEND_PROXY_TARGET=http://localhost:18081 npm run dev
```

## 如何运行测试和构建

```bash
cd backend && ./mvnw test -B
cd frontend && npm ci
cd frontend && npm test
cd frontend && npm run build
cd frontend && npm audit --audit-level=high
python3 scripts/agent_changelog_archive.py
python3 scripts/agent_doc_check.py
git diff --check
```

注意：当前 `scripts/agent_doc_check.py` 会因 backlog 无 `todo` 返回失败，这是自主开发协议中的阶段停止条件，不代表代码构建失败。
