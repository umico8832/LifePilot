# Current State

## 当前阶段

P3-003 完成。Phase 10（购物统计接口）完成。

## 当前最高优先级任务

P3-004 后端 Service 层补充测试。

## 最近完成任务

- P0-001～P0-013：全部完成。
- P1-001～P1-017：全部完成。
- P2-001～P2-004：全部完成。
- P3-001 前端首页统计图表可视化：新建 EChart.vue 通用组件，HomeView 集成 3 个 ECharts 图表。
- P3-002 实现分类财务统计接口：新增 `FinanceCategoriesResponse` DTO（含 expenseCategories 和 incomeCategories 分组），`StatisticService.getFinanceCategories` 按月按类型分组统计分类汇总并按金额降序排列，`StatisticController` 新增 `GET /finance/categories?year=&month=` 端点，前端 `statistics.ts` 新增 `FinanceCategoriesResponse` 类型和 `getFinanceCategories` 函数。
- P3-003 实现购物统计接口：新增 `ShoppingStatsResponse` DTO（含 totalLists、activeLists、completedLists、totalItems、purchasedItems、recent30Days 趋势），`StatisticService.getShoppingStats` 统计购物清单状态、物品采购比和近 30 天创建趋势，`StatisticController` 新增 `GET /shopping` 端点，前端 `statistics.ts` 新增 `ShoppingStatsResponse` 类型和 `getShoppingStats` 函数，新增 3 个单元测试。

## 当前阻塞项

无。

## 下一项自动任务

P3-004 后端 Service 层补充测试。

## 最近验证结果

- 后端 `./mvnw test`：通过，121 tests passed。
- 前端 `npm test`：通过，3 个测试文件共 24 项测试全部通过。
- 前端 `npm run build`：通过。
- Flyway 迁移 V1-V8 在 H2 测试数据库上通过。
- 文档归档脚本 `python3 scripts/agent_changelog_archive.py`：通过。
- 文档一致性检查 `python3 scripts/agent_doc_check.py`：通过。

## 注意事项

- GlobalExceptionHandler 已统一处理所有常见异常类型。
- 所有错误响应使用统一 `ApiResponse.error(code, message)` 结构。
- BusinessException 默认映射为 400 Bad Request。
- 不要提交真实 `.env` 或真实密钥。
- 本机 `8080` 和 `3306` 端口被占用，当前开发服务使用后端 `18081`、MySQL `3307` 和前端 `5173`。
- Maven wrapper 已生成（`backend/mvnw`），CI 使用 `./mvnw test -B`。
- Swagger UI 可通过 `/swagger-ui.html` 访问（无需认证），OpenAPI JSON 可通过 `/v3/api-docs` 获取。
- ECharts 6.1.0 已安装，使用 tree-shaking 方式导入。