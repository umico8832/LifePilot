# Current State

## 当前阶段

P6-001 完成。当前处于 P6 智能饮食阶段（Phase 18）；最近完成 AI 根据库存推荐菜谱功能。

## 当前最高优先级任务

无待定任务（P6-001 已完成，待规划下一阶段）。

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
- P4-003 待办完成率统计接口：`TodoStatsResponse` 新增 `completionRate`（完成率）和 `recent30Days`（30 天完成趋势）字段；`StatisticService.getTodoStats` 增加完成率计算和按日统计完成数逻辑；新增 3 个单元测试；前端 `statistics.ts` 新增 `TodoDailyTrend` 接口和字段；`HomeView` 新增完成率环形图（SVG）和近 30 天完成趋势柱状图。后端总测试从 215 增长到 217。
- P4-004 购物预算估算功能：后端 `estimatedBudget` 字段已在 V4 迁移、`ShoppingList` 实体、DTO（Create/Update/Response）和 `ShoppingService` 中完整实现。前端 `shopping.ts` API 类型已包含 `estimatedBudget`；`ShoppingView.vue` 新增预算对比卡片组件（`getTotalEstimatedCost` / `getBudgetPercent` 计算函数、进度条、预算/花费/剩余/百分比展示、超预算红色提示）。无需新增 V9 迁移。
- P4-005 前端视图层组件测试：新建 `frontend/src/views/__tests__/` 目录，为 AuthView 新增 13 个组件测试（表单提交、登录/注册模式切换、错误消息显示、重定向逻辑），为 FinanceView 新增 12 个组件测试（无空间状态、数据加载、错误态、空态、汇总计算、工具栏按钮）。前端总测试从 66 增长到 91。
- P5-001 实现一周饮食计划 CRUD：新建 `V9__create_meal_plan.sql` 迁移（meal_plan 表含 household_id、recipe_id、planned_date、meal_type、note 等字段）；后端 `recipe` 模块新增 `MealPlan` 实体、`MealPlanMapper`、`MealPlanService`、`MealPlanController` 和 3 个 DTO（`CreateMealPlanRequest`、`UpdateMealPlanRequest`、`MealPlanResponse`）；前端新建 `mealplan.ts` API 模块、`MealPlanView.vue` 周历视图页面（7 天 × 4 餐次网格、周导航、今日高亮、菜谱选择弹窗）；路由新增 `/mealplan`、AppShell 导航栏新增"饮食计划"入口；新增 13 个 MealPlanService 单元测试。后端总测试从 217 增长到 230。
- P6-001 AI 根据库存推荐菜谱：后端 `AiProvider` 接口新增 `recommendRecipes(List<InventoryItem>, List<Recipe>)` 方法；新增 `RecipeRecommendationResponse` DTO（含 `RecommendedRecipe` 子记录：recipeId、recipeName、matchedIngredients、missingIngredients、matchScore、reason）；`MockAiProvider` 实现确定性关键词匹配评分逻辑（解析 `ingredientsJson` 中食材名称，与库存物品名称双向包含匹配，按匹配度排序）；`OpenAiProvider` 委托 Mock 实现；`AiService.recommendRecipes()` 查询空间库存和菜谱后委托 provider 计算；`AiController` 新增 `GET /api/ai/spaces/{spaceId}/recommend-recipes` 端点；前端 `ai.ts` 新增 `RecommendedRecipe`、`RecipeRecommendation` 类型和 `recommendRecipes()` 方法；前端 `MealPlanView.vue` 新增"AI 菜谱推荐"按钮和推荐面板（匹配百分比进度条、已匹配/缺失食材列表、推荐理由，点击可快速填入饮食计划）；新增 4 个 AiService 单元测试。后端单元测试 13 通过，前端构建和测试通过。

## 当前阻塞项

无。

## 下一项自动任务

待定（P6-001 已完成）。

## 最近验证结果

- 后端 `./mvnw test -B -Dtest="AiServiceTests"`：通过，13 tests passed（含 P6-001 新增 4 个）。
- 前端 `npm test`：通过，11 个测试文件共 91 项测试全部通过。
- 前端 `npm run build`：通过。
- 注：集成测试（`@SpringBootTest` 类）因 P5-001 遗留的 H2 Flyway `meal_plan` 表映射问题持续失败（`Cannot resolve reference to bean 'sqlSessionTemplate'`），为 P5-001 已知遗留问题，非 P6-001 引入。
- 文档归档脚本 `python3 scripts/agent_changelog_archive.py`：待运行。
- 文档一致性检查 `python3 scripts/agent_doc_check.py`：待运行。

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
- 购物清单 `estimated_budget` 列在 V4 迁移中已存在，无需 V9 迁移。
- 饮食计划 `meal_plan` 表由 V9 迁移创建，包含外键约束关联 `household`、`recipe` 和 `users` 表。
- `AiProvider` 接口新增 `recommendRecipes` 方法；`RecipeRecommendationResponse` DTO 新增于 `ai/dto/`。
- `AiService` 构造函数新增 `RecipeMapper` 依赖。
- 集成测试（`@SpringBootTest`）因 P5-001 遗留的 H2/Flyway 兼容问题仍失败，单元测试全部通过。
