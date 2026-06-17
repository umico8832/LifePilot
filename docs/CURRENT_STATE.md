# Current State

## 当前阶段

Phase 2：用户与鉴权。Phase 0 文档体系、Phase 1 项目骨架、P0-005 用户注册登录已完成。

## 当前最高优先级任务

P0-006 实现生活空间模型。

## 最近完成任务

- P0-001 创建项目文档体系。
- P0-002 创建项目骨架。
- P0-003 配置后端 Spring Boot。
- P0-004 配置前端 Vue 3。
- P0-005 实现用户注册登录。
- 新增项目 Agent skills：自主开发、后端模块、API/数据契约、AI mock provider、前端设计、本地 Web 验证、文档协作。

## 当前阻塞项

无。未接入真实外部服务，不需要真实 API Key。

## 下一项自动任务

P0-006 实现生活空间模型。

## 最近验证结果

- 文档完整性检查：通过。
- 后端 `mvn test`：通过，2 tests passed。
- 前端 `npm install && npm run build`：通过。
- 前端 `npm audit --audit-level=high`：通过，0 vulnerabilities。
- 本地运行验证：后端在 `18081` 启动通过；前端在 `5173` 启动通过；浏览器验证首页显示 `lifepilot-backend UP` 且无 console error。
- 用户鉴权验证：后端 `mvn test` 通过，5 tests passed；真实 MySQL curl 注册、登录、`/api/users/me` 通过；浏览器 UI 注册通过并显示当前用户。
- 项目 skills 验证：`quick_validate.py` 对 `agent-skills/*` 全部通过；`lifepilot-webapp-testing/scripts/with_server.py --help` 可运行。

## 注意事项

- 当前只完成骨架和健康检查，不要声称业务模块已实现。
- 当前已完成用户注册、登录、JWT 鉴权和当前用户接口；生活空间尚未实现。
- 本机 `8080` 和 `3306` 端口被占用，当前开发服务使用后端 `18081`、MySQL `3307` 和前端 `5173`。
- 前端构建存在大 chunk 提示，当前阶段不阻塞，后续页面增多时应做按路由分包。
- AI provider 当前只在文档中设计，业务实现阶段必须先做 mock。
- 不要提交真实 `.env` 或真实密钥。
- 任务匹配时先阅读 `agent-skills/*/SKILL.md`，再按 `AGENTS.md` 继续阅读专项文档。
