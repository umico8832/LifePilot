# Agent Changelog

本文件只保留最近若干条完整开发记录；更早历史由 `scripts/agent_changelog_archive.py` 自动归档到 `docs/changelog/`。

默认接手请读 `docs/RECENT_HISTORY.md`。需要追溯具体历史时，再按月份查看归档文件。

## 维护方式

```bash
python3 scripts/agent_changelog_archive.py --keep 10
```

脚本默认保留最近 10 条完整记录，并刷新 `docs/RECENT_HISTORY.md`。

## 2026-06-20 22:26 Asia/Shanghai 修复饮食计划 Mapper 注册导致的服务启动失败

- 任务：修复实际体验验证发现的后端启动阻断问题。
- 改动：为 `backend/src/main/java/com/lifepilot/recipe/MealPlanMapper.java` 补充 MyBatis `@Mapper` 注解，使其与其余 Mapper 的注册方式一致。
- 影响：修复 `MealPlanService` 无法注入 `MealPlanMapper` 的问题；恢复所有 `@SpringBootTest` 控制器测试和真实 MySQL 环境中的后端服务启动。

**验证**：
- `cd backend && ./mvnw test -B`：234 tests passed。
- MySQL（3307）联调下，后端（18081）启动成功，`GET /api/health` 返回 `UP`。
- 浏览器：完成注册、自动登录、创建家庭空间；未发现 console error。

---

## 2026-06-19 20:25 Asia/Shanghai P6-001 AI 根据库存推荐菜谱

- **任务**：P6-001 AI 根据库存推荐菜谱
- **改动**：
  - 后端：`AiProvider` 接口新增 `recommendRecipes(List<InventoryItem>, List<Recipe>)` 方法；新增 `RecipeRecommendationResponse` DTO（含 `RecommendedRecipe` 子记录）；`MockAiProvider` 实现确定性关键词匹配评分逻辑（解析 `ingredientsJson` 中食材名称与库存双向包含匹配，按匹配度排序）；`OpenAiProvider` 委托 Mock 实现；`AiService.recommendRecipes()` 查询空间库存和菜谱后委托 provider 计算；`AiController` 新增 `GET /api/ai/spaces/{spaceId}/recommend-recipes` 端点；`AiService` 构造函数新增 `RecipeMapper` 依赖
  - 前端：`ai.ts` 新增 `RecommendedRecipe`、`RecipeRecommendation` 类型和 `recommendRecipes()` API 方法；`MealPlanView.vue` 新增"AI 菜谱推荐"按钮和推荐面板（匹配百分比进度条、已匹配/缺失食材列表、推荐理由，点击可快速填入饮食计划）
  - 测试：AiServiceTests 新增 4 个单元测试（`recommendRecipes_returnsProviderRecommendation`、`recommendRecipes_throwsWhenNotMember`、`recommendRecipes_passesInventoryAndRecipesToProvider`、`recommendRecipes_emptyRecipesWhenNoData`）
- **验证**：`cd backend && ./mvnw test -B -Dtest="AiServiceTests"`：13 tests passed；`cd frontend && npm run build`：通过；`cd frontend && npm test`：91 tests passed
- **文档更新**：BACKLOG.md、CURRENT_STATE.md、CHANGELOG_AGENT.md、ROADMAP.md

## 2026-06-19 17:26 Asia/Shanghai P5-001 实现一周饮食计划 CRUD

- **任务**：P5-001 实现一周饮食计划 CRUD
- **改动**：
  - 后端：新建 `V9__create_meal_plan.sql` 迁移（meal_plan 表含 household_id、recipe_id、planned_date、meal_type、note 等字段）；recipe 模块新增 MealPlan 实体、MealPlanMapper、MealPlanService、MealPlanController 和 3 个 DTO
  - 前端：新建 `mealplan.ts` API 模块、`MealPlanView.vue` 周历视图页面（7 天 × 4 餐次网格、周导航、今日高亮、菜谱选择弹窗）；路由新增 `/mealplan`；AppShell 导航栏新增"饮食计划"入口
  - 测试：新增 13 个 MealPlanService 单元测试
- **验证**：
  - `cd backend && ./mvnw test -Dtest="MealPlanServiceTests"`：13 tests passed
  - `cd frontend && npm test`：11 文件 91 tests passed
  - `cd frontend && npm run build`：通过

---

## 2026-06-19 16:50 Asia/Shanghai P4-005 前端视图层组件测试

- **任务**：P4-005 前端视图层组件测试
- **改动**：
  - 新建 `frontend/src/views/__tests__/` 目录
  - 为 AuthView 新增 13 个组件测试：表单提交、登录/注册模式切换、错误消息显示、重定向逻辑、返回导航
  - 为 FinanceView 新增 12 个组件测试：无空间状态、数据加载/失败/空态、汇总计算、工具栏按钮、空间选择器
  - 前端总测试从 66 增长到 91（11 个测试文件）
  - 更新 `docs/TESTING.md` 覆盖范围描述
- **验证**：
  - `cd frontend && npm test`：11 文件 91 tests passed
  - `cd frontend && npm run build`：通过

---

## 2026-06-19 15:53 Asia/Shanghai P4-004 购物预算估算功能

- 任务：P4-004 购物预算估算功能
- 改动：
  - 后端：`estimatedBudget` 字段已在 V4 迁移（`estimated_budget DECIMAL(12,2) NULL`）、`ShoppingList` 实体、`CreateShoppingListRequest`（`@Positive BigDecimal`）、`UpdateShoppingListRequest`、`ShoppingListResponse`（含 `from()` 方法）和 `ShoppingService`（create/update 逻辑）中完整实现，无需新增 V9 迁移
  - 前端：`ShoppingView.vue` 新增 `getTotalEstimatedCost()`（累加 items 的 estimatedPrice × quantity）和 `getBudgetPercent()` 计算函数；模板新增预算对比卡片（进度条 + 预算/花费/剩余/百分比，超预算时进度条变红）；列表页已有预算列展示
- 文档：`docs/BACKLOG.md` P4-004 状态更新为 done；`docs/CURRENT_STATE.md` 更新当前状态

**验证**：
- `cd backend && ./mvnw test -B`：217 tests passed
- `cd frontend && npm test`：66 tests passed
- `cd frontend && npm run build`：通过

---

## 2026-06-19 15:48 Asia/Shanghai P4-003 待办完成率统计接口

- 任务：P4-003 待办完成率统计接口
- 改动：
  - 后端：`TodoStatsResponse` 新增 `completionRate`（double，完成率 = completed / (total - cancelled)）和 `recent30Days`（`List<DailyTrend>`，近 30 天每天完成任务数）；内部新增 `DailyTrend` record；`StatisticService.getTodoStats` 增加完成率计算和按 `updatedAt` 统计每日完成数的逻辑；`StatisticServiceTests` 更新 2 个现有测试并新增 3 个测试（完成率含取消任务、仅取消任务时完成率为 0、30 天趋势验证），后端总测试从 215 增长到 217
  - 前端：`statistics.ts` 新增 `TodoDailyTrend` 接口，`TodoStatsResponse` 新增 `completionRate` 和 `recent30Days` 字段；`HomeView` 新增 `todoTrendOption`（近 30 天完成趋势柱状图）、`todoCompletionPercent` 计算属性，模板新增完成率环形图（SVG `stroke-dasharray` 实现）和趋势图表卡片，新增 `.completion-rate-display` / `.completion-ring` / `.dot.*` 样式
- 文档：`docs/API_DESIGN.md` 更新 todos 统计接口描述；`docs/BACKLOG.md` P4-003 状态更新为 done；`docs/CURRENT_STATE.md` 更新当前状态

**验证**：
- `cd backend && ./mvnw test -B`：217 tests passed（原 215 新增 2，含更新）
- `cd frontend && npm test`：66 tests passed
- `cd frontend && npm run build`：通过

---

## 2026-06-19 15:30 Asia/Shanghai P4-002 库存临期和缺货提醒逻辑

- 任务：P4-002 库存临期和缺货提醒逻辑
- 改动：
  - 后端：新增 `InventoryAlertsResponse` DTO（含 `AlertItem` 内嵌 record）；`StatisticService.getInventoryAlerts` 方法查询 7 天内临期物品和低库存物品（quantity ≤ threshold）；`StatisticController` 新增 `GET /api/spaces/{spaceId}/statistics/inventory/alerts` 端点；`StatisticServiceTests` 新增 5 个测试用例覆盖空数据、临期物品、低库存物品、混合提醒和已过期物品排除
  - 前端：`statistics.ts` 新增 `InventoryAlertsResponse`/`InventoryAlertItem` 类型和 `getInventoryAlerts` API 方法，修复 `CategoryCount` 类型缺失和 `ApiResponse` 导入问题；`HomeView` 集成库存提醒卡片列表（expiring/low_stock 双类型，含 badge、详情、响应式布局）

**验证**：
- `cd backend && ./mvnw test -B`：215 tests passed（原 210 新增 5）
- `cd frontend && npm test`：66 tests passed
- `cd frontend && npm run build`：通过

**状态**：done

## 2026-06-19 15:10 Asia/Shanghai P4-001 实现用户个人资料编辑

- 任务：P4-001 实现用户个人资料编辑
- 改动：
  - 后端：新增 `ProfileUpdateRequest` DTO（含 Jakarta Validation）；`UserService.updateProfile` 方法支持 displayName 和 avatarUrl 更新、trim 和置空处理；`UserController` 新增 `PUT /api/users/me` 端点
  - 前端：`auth.ts` 新增 `updateProfile` API 函数；`auth` store 新增 `updateProfile` action；新建 `ProfileView.vue` 个人设置页面（头像预览、表单编辑、成功/错误提示）；路由和导航栏注册"设置"入口（Settings 图标）
  - 测试：新增 `UserServiceTests`（12 用例）覆盖 updateProfile 正常路径、异常路径、trim/置空逻辑、requireById 和 findByEmail

**验证**：
- `cd backend && ./mvnw test -B`：210 tests passed（原 198 新增 12）
- `cd frontend && npm test`：66 tests passed
- `cd frontend && npm run build`：通过

**状态**：done

## 2026-06-19 14:50 Asia/Shanghai P3-005 前端 API 层测试补充

- 任务：P3-005 前端 API 层测试补充
- 改动：
  - 新增 6 个 API 测试文件：`statistics.test.ts`（7 tests）、`shopping.test.ts`（9 tests）、`inventory.test.ts`（7 tests）、`todo.test.ts`（7 tests）、`document.test.ts`（7 tests）、`ai.test.ts`（5 tests）
  - 覆盖所有 API 模块的请求路径、参数传递、响应处理和错误传播
  - 使用 vitest 的 `vi.mock` mock `http` 模块，避免真实网络请求
**验证**：
- `cd frontend && npm test`：9 文件 66 tests passed（原 24 新增 42）
- `cd frontend && npm run build`：通过

**状态**：done

## 2026-06-19 12:34 Asia/Shanghai P3-004 后端 Service 层补充测试

**任务**：P3-004 后端 Service 层补充测试

**改动**：
- 新增 `TransactionServiceTests`（12 用例）：覆盖 create 默认值、create 显式类型货币、非成员拒绝、list 正常和空、get 存在/不存在/错误空间、update 正常/不存在、delete 正常/不存在。
- 新增 `ShoppingServiceTests`（13 用例）：覆盖 createList、listLists、getList 存在/不存在/错误空间/含子项、updateList、deleteList 正常/不存在、addItem、updateItem 不存在/错误清单、deleteItem 正常/不存在。
- 新增 `InventoryServiceTests`（12 用例）：覆盖 createItem 正常/默认数量/非成员、listItems 正常/空、getItem 存在/不存在/错误空间、updateItem 正常/不存在、deleteItem 正常/不存在。
- 新增 `TodoServiceTests`（15 用例）：覆盖 createTask 默认优先级/显式优先级/无效优先级/非成员、listTasks 无筛选/有筛选、getTask 存在/不存在/错误空间、updateTask 正常/无效状态/无效优先级/不存在、deleteTask 正常/不存在。
- 新增 `RecipeServiceTests`（11 用例）：覆盖 createRecipe 正常/非成员、listRecipes 正常/空、getRecipe 存在/不存在/错误空间、updateRecipe 正常/不存在、deleteRecipe 正常/不存在。
- 新增 `DocumentServiceTests`（14 用例）：覆盖 createDocument 正常/无效类型/非成员、listDocuments 正常/类型筛选/空、getDocument 存在/不存在/错误空间、updateDocument 正常/无效类型/不存在、deleteDocument 正常/不存在。
- 使用 `(EntityType) any()` 显式类型转换解决 MyBatis-Plus BaseMapper 方法重载歧义。

**验证**：
- `cd backend && ./mvnw test -B`：通过，198 tests passed（原 121 + 新增 77）。
- `python3 scripts/agent_changelog_archive.py`：通过。
- `python3 scripts/agent_doc_check.py`：通过。
