# Current State

## 当前阶段

P1-015 完成。Phase 0-15 全部完成。

## 当前最高优先级任务

P1-016 前端分类管理 UI 集成。

## 最近完成任务

- P0-001～P0-013：全部完成。
- P1-001～P1-015：全部完成。
- P1-015 增加更多统计接口：InventoryStatsResponse DTO（totalItems、lowStockCount、byCategory）+ TodoStatsResponse DTO（按状态分计数、overdueCount）+ StatisticService 新增 TodoTaskMapper 注入和 `getInventoryStats`/`getTodoStats` 方法 + StatisticController GET `/statistics/inventory` 和 `/statistics/todos` + 前端 `statistics.ts` 新增类型和函数 + 修复 MockAiProvider PRIORITY_KEYWORDS 为 LinkedHashMap 确保迭代顺序确定性 + 修复 parseTodoWithPriorityKeyword 测试。

## 当前阻塞项

无。

## 下一项自动任务

P1-016 前端分类管理 UI 集成。

## 最近验证结果

- 后端 `./mvnw test`：通过，88 tests passed（含 22 项 AiControllerTests）。
- 前端 `npm run build`：通过（vue-tsc + vite build）。
- Flyway 迁移 V1-V8 在 H2 测试数据库上通过。
- 文档一致性检查：待运行。

## 注意事项

- GlobalExceptionHandler 已统一处理所有常见异常类型。
- 所有错误响应使用统一 `ApiResponse.error(code, message)` 结构。
- BusinessException 默认映射为 400 Bad Request。
- 不要提交真实 `.env` 或真实密钥。
- 本机 `8080` 和 `3306` 端口被占用，当前开发服务使用后端 `18081`、MySQL `3307` 和前端 `5173`。
- Maven wrapper 已生成（`backend/mvnw`），CI 使用 `./mvnw test -B`。
- Swagger UI 可通过 `/swagger-ui.html` 访问（无需认证），OpenAPI JSON 可通过 `/v3/api-docs` 获取。
