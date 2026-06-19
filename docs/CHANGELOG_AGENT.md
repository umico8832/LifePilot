# Agent Changelog

本文件只保留最近若干条完整开发记录；更早历史由 `scripts/agent_changelog_archive.py` 自动归档到 `docs/changelog/`。

默认接手请读 `docs/RECENT_HISTORY.md`。需要追溯具体历史时，再按月份查看归档文件。

## 维护方式

```bash
python3 scripts/agent_changelog_archive.py --keep 10
```

脚本默认保留最近 10 条完整记录，并刷新 `docs/RECENT_HISTORY.md`。

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

## 2026-06-19 12:18 Asia/Shanghai 修复 Agent 文档漂移检查和历史摘要生成

**任务**：修复 Agent 文档漂移检查和历史摘要生成

**改动**：
- `docs/API_DESIGN.md` 标记 `/statistics/inventory` 和 `/statistics/todos` 为已完成，并补充返回内容说明，避免与 P1-015 done 状态冲突。
- `docs/CURRENT_STATE.md` 将当前阶段改为 P3 质量补强阶段描述，避免 Phase 10 完成与下一任务 Phase 12 并存时产生误解。
- `docs/ROADMAP.md` 补齐 Phase 13、Phase 14、Phase 15，覆盖 BACKLOG 已引用的前端测试体系、真实 AI Provider 准备、可视化体验增强。
- `scripts/agent_changelog_archive.py` 兼容旧格式 `- Agent 任务名称：` 和新格式 `**任务**：` / `**验证**：`，并清理误混入条目的重复维护块。
- `scripts/agent_doc_check.py` 新增三类防漂移检查：RECENT_HISTORY 占位符、BACKLOG Phase 是否都在 ROADMAP 定义、P1-015 完成后 API 统计接口是否标 ✅。

**验证**：
- `python3 -m py_compile scripts/agent_changelog_archive.py scripts/agent_doc_check.py`：通过。
- `python3 scripts/agent_changelog_archive.py`：通过，RECENT_HISTORY 已重新生成且无占位符。
- `python3 scripts/agent_doc_check.py`：通过。
- `git diff --check`：通过。

**文档更新**：`docs/API_DESIGN.md`、`docs/CURRENT_STATE.md`、`docs/ROADMAP.md`、`docs/RECENT_HISTORY.md`、`docs/CHANGELOG_AGENT.md`。

## 2026-06-19 11:46 Asia/Shanghai P3-003 实现购物统计接口

**任务**：P3-003 实现购物统计接口

**改动**：
- 新增 `ShoppingStatsResponse` DTO：返回 `totalLists`、`activeLists`、`completedLists`、`totalItems`、`purchasedItems`、`recent30Days`（30 天每天创建清单数趋势），使用 Java record + 内嵌 `DailyTrend` record。
- `StatisticService` 新增 `getShoppingStats` 方法：查询空间内所有 `ShoppingList`，按状态统计 active/completed；查询关联 `ShoppingItem` 统计已采购数量；按近 30 天日期聚合每日创建清单数。
- `StatisticController` 新增 `GET /api/spaces/{spaceId}/statistics/shopping`。
- 前端 `statistics.ts` 新增 `ShoppingStatsResponse` 类型和 `getShoppingStats` API 方法。
- `StatisticServiceTests` 新增 3 个测试：空数据返回零值、按状态正确计数（active/completed/cancelled）、30 天趋势正确聚合。
- `docs/API_DESIGN.md` 为 `/statistics/shopping` 标记 ✅。

**验证**：
- 后端 `./mvnw test`：121 tests passed，无回归。
- 前端 `npm run build` + `npm test`：24 tests passed，无回归。

**文档更新**：`BACKLOG.md`、`CURRENT_STATE.md`、`CHANGELOG_AGENT.md`、`API_DESIGN.md`。

---

## 2026-06-18 23:48 Asia/Shanghai P3-002 实现分类财务统计接口

**任务**：P3-002 实现分类财务统计接口

**改动**：
- 新增 `FinanceCategoriesResponse` DTO：返回 `year`、`month`、`totalIncome`、`totalExpense`、`expenseCategories`、`incomeCategories`，分类列表按金额降序。
- `StatisticService` 新增 `getFinanceCategories` 方法：查询当月 `TransactionRecord`，按收入/支出分别汇总分类，使用 `buildCategoryDetails` 统一组装排序结果。
- `StatisticController` 新增 `GET /api/spaces/{spaceId}/statistics/finance/categories?year=&month=`。
- 前端 `statistics.ts` 新增 `CategoryDetail`、`FinanceCategoriesResponse` 类型以及 `getFinanceCategories` API 方法。
- `docs/API_DESIGN.md` 为 `/statistics/finance/categories` 标记 ✅。

**验证**：
- 后端 `./mvnw test`：118 tests passed，无回归。
- 前端 `npm run build` + `npm test`：24 tests passed，无回归。

**文档更新**：
- `docs/BACKLOG.md`、`docs/CURRENT_STATE.md`、`docs/API_DESIGN.md`、`docs/CHANGELOG_AGENT.md`：本条。

## 2026-06-18 23:44 Asia/Shanghai P3-001 前端首页统计图表可视化

**任务**：P3-001 前端首页统计图表可视化

**改动**：
- 新建 `frontend/src/components/EChart.vue`：通用 ECharts 包装组件，基于 echarts/core tree-shaking 导入（BarChart、LineChart、PieChart + TooltipComponent、LegendComponent、GridComponent、CanvasRenderer），支持 `option` prop 深度监听和窗口 resize 自动适配，组件卸载时 dispose 释放资源。
- 更新 `frontend/src/views/HomeView.vue`：
  - 新增 `loadCharts()` 方法，调用 `getFinanceMonthly` 和 `getTodoStats` 获取当月财务分类和待办状态数据。
  - 新增 3 个 computed 图表 option：`barChartOption`（月度收支概览柱状图）、`pieChartOption`（支出分类饼图）、`todoChartOption`（待办状态饼图）。
  - 模板新增 charts-section 区域，3 个 chart-card 渲染 EChart 组件。
  - 新增 chart-grid/chart-card/chart-title 样式，支持响应式布局。
  - 空数据时图表自动隐藏（computed 返回 null）。
- 同步规划 P3 阶段任务（P3-001～P3-005）写入 BACKLOG。

**验证**：
- 后端 `./mvnw test`：118 tests passed，无回归。
- 前端 `npm test`：24 tests passed，无回归。
- 前端 `npm run build`：通过，ECharts chunk 超 500kB 警告为已知（ECharts 库本身体积）。

**文档更新**：
- `docs/BACKLOG.md`：P3-001 标记 done，新增 P3-002～P3-005 任务。
- `docs/CURRENT_STATE.md`：更新当前阶段和下一项任务。
- `docs/CHANGELOG_AGENT.md`：本条。
