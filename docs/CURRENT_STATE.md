# Current State

## 当前阶段

P1-010 完成。Phase 0-10, 12, P1-001～P1-010 全部完成。

## 当前最高优先级任务

待确定（从 BACKLOG 中选取下一个 P1 任务）。

## 最近完成任务

- P0-001～P0-013：全部完成。
- P1-001 完善错误码和异常处理（GlobalExceptionHandler 新增 5 类异常处理器 + 5 项测试）。
- P1-002 增加 GitHub Actions CI。
- P1-003 增加 OpenAPI 文档。
- P1-004 完善前端空态和错误态。
- P1-005 增加前端路由守卫和未登录重定向（`router.beforeEach` 鉴权守卫 + `requiresAuth` / `guestOnly` meta + 401 响应拦截器自动登出 + AuthView 登录后跳回 `redirect`）。
- P1-006 前端大 chunk 分包（所有路由改为 `() => import(...)` 懒加载，最大 chunk 从 1,090 kB 降至 117 kB）。
- P1-007 完善 AppShell 导航栏（导航项改为路由链接 + 当前路由高亮 + lucide 图标 + 底部用户名称 + 退出登录按钮 + 侧边栏 footer 样式）。
- P1-008 前端视口宽度适配完善（4 断点响应式：1024px 双列 grid、900px 侧边栏→顶栏水平导航图标+文字、768px SpaceView 双栏→单栏、560px 手机图标导航、所有表格 `.table-scroll` 防溢出、工具栏 flex-wrap、Dialog 92vw 上限）。
- P1-009 实现生活待办 CRUD（Phase 7）：Flyway V6 迁移 `todo_task` 表 + TodoTask 实体 + MyBatis-Plus Mapper + TodoService（状态/priority 枚举校验 + 按状态筛选）+ TodoController `/api/spaces/{spaceId}/todo-tasks` + 7 项测试 + 前端 API 客户端 `todo.ts` + TodoView.vue（状态筛选、优先级/逾期标识、状态流转按钮、空态/错误态）+ 路由 `/todo` + AppShell 导航待办入口。
- P1-010 实现菜谱管理 CRUD（Phase 8）：Flyway V7 迁移 `recipe` 表 + Recipe 实体 + MyBatis-Plus Mapper + RecipeService + RecipeController `/api/spaces/{spaceId}/recipes` + 7 项测试 + 前端 API 客户端 `recipe.ts` + RecipeView.vue（食材解析显示、空态/错误态）+ 路由 `/recipe` + AppShell 导航菜谱入口。

## 当前阻塞项

无。

## 下一项自动任务

待确定：从 `docs/BACKLOG.md` 选取下一个最高优先级 P1 todo 任务。

## 最近验证结果

- 后端 `./mvnw test`：通过，67 tests passed（含 7 项 RecipeControllerTests）。
- 前端 `npm run build`：通过（vue-tsc + vite build）。
- Flyway 迁移 V1-V7 在 H2 测试数据库上通过。

## 注意事项

- GlobalExceptionHandler 已统一处理所有常见异常类型。
- 所有错误响应使用统一 `ApiResponse.error(code, message)` 结构。
- 不要提交真实 `.env` 或真实密钥。
- 本机 `8080` 和 `3306` 端口被占用，当前开发服务使用后端 `18081`、MySQL `3307` 和前端 `5173`。
- Maven wrapper 已生成（`backend/mvnw`），CI 使用 `./mvnw test -B`。
- Swagger UI 可通过 `/swagger-ui.html` 访问（无需认证），OpenAPI JSON 可通过 `/v3/api-docs` 获取。