# Current State

## 当前阶段

P1-011 完成。Phase 0-11 全部完成。

## 当前最高优先级任务

待确定（从 BACKLOG 中选取下一个 P1 任务）。剩余 P1 任务包括：AI mock provider 扩展（待办/购物清单/月报）、更多统计接口、前端交互优化等。

## 最近完成任务

- P0-001～P0-013：全部完成。
- P1-001～P1-010：全部完成。
- P1-011 实现票据与文件管理 CRUD（Phase 11）：Flyway V8 迁移 `document_record` 表 + DocumentRecord 实体 + MyBatis-Plus Mapper + DocumentService（类型校验 + 按类型筛选 + 即将过期标识）+ DocumentController `/api/spaces/{spaceId}/documents` + 7 项测试 + 前端 API 客户端 `document.ts` + DocumentView.vue（类型筛选、到期提醒标识、空态/错误态）+ 路由 `/document` + AppShell 导航文档入口。

## 当前阻塞项

无。

## 下一项自动任务

待确定：从 `docs/BACKLOG.md` 选取下一个最高优先级 P1 todo 任务。

## 最近验证结果

- 后端 `./mvnw test`：通过，74 tests passed（含 7 项 DocumentControllerTests）。
- 前端 `npm run build`：通过（vue-tsc + vite build）。
- Flyway 迁移 V1-V8 在 H2 测试数据库上通过。

## 注意事项

- GlobalExceptionHandler 已统一处理所有常见异常类型。
- 所有错误响应使用统一 `ApiResponse.error(code, message)` 结构。
- BusinessException 默认映射为 400 Bad Request。
- 不要提交真实 `.env` 或真实密钥。
- 本机 `8080` 和 `3306` 端口被占用，当前开发服务使用后端 `18081`、MySQL `3307` 和前端 `5173`。
- Maven wrapper 已生成（`backend/mvnw`），CI 使用 `./mvnw test -B`。
- Swagger UI 可通过 `/swagger-ui.html` 访问（无需认证），OpenAPI JSON 可通过 `/v3/api-docs` 获取。