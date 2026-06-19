# Current State

## 当前阶段

P4-002 完成。当前处于 P4 质量增强阶段（Phase 16）；最近完成库存临期和缺货提醒功能。

## 当前最高优先级任务

P4-003 待办完成率统计接口（todo）。

## 最近完成任务

- P0-001～P0-013：全部完成。
- P1-001～P1-017：全部完成。
- P2-001～P2-004：全部完成。
- P3-001 前端首页统计图表可视化：新建 EChart.vue 通用组件，HomeView 集成 3 个 ECharts 图表。
- P3-002 实现分类财务统计接口：新增 `FinanceCategoriesResponse` DTO，`StatisticService.getFinanceCategories` 按月按类型分组统计，`StatisticController` 新增端点。
- P3-003 实现购物统计接口：新增 `ShoppingStatsResponse` DTO 和 `/shopping` 端点。
- P3-004 后端 Service 层补充测试：为 6 个 Service 新增 75 个 Mockito 单元测试，后端总测试从 121 增长到 198。
- P3-005 前端 API 层测试补充：为 statistics、shopping、inventory、todo、document、ai 6 个 API 模块新增 42 个测试用例，前端总测试从 24 增长到 66。
- P4-001 实现用户个人资料编辑：后端 `PUT /api/users/me` 端点、`UserService.updateProfile` 方法、`ProfileUpdateRequest` DTO；前端 `ProfileView.vue` 页面、auth store `updateProfile` action；新增 12 个 UserService 单元测试，后端总测试从 198 增长到 210。
- P4-002 库存临期和缺货提醒逻辑：后端新增 `InventoryAlertsResponse` DTO、`StatisticService.getInventoryAlerts` 方法（7 天内临期 + 低库存筛选）、`StatisticController` 新增 `GET /api/spaces/{spaceId}/statistics/inventory/alerts` 端点、5 个单元测试；前端 `statistics.ts` 新增 API 类型和方法、`HomeView` 集成库存提醒卡片列表。后端总测试从 210 增长到 215。

## 当前阻塞项

无。

## 下一项自动任务

P4-003 待办完成率统计接口。

## 最近验证结果

- 后端 `./mvnw test`：通过，215 tests passed。
- 前端 `npm test`：通过，9 个测试文件共 66 项测试全部通过。
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
- MyBatis-Plus `BaseMapper` 的 `insert(T)/insert(Collection<T>)`、`updateById(T)/updateById(Collection<T>)`、`deleteById(Serializable)/deleteById(T)` 存在重载歧义，Mockito 测试中需使用 `(EntityType) any()` 显式类型转换，避免编译错误。