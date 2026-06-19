# Agent Changelog

本文件只保留最近若干条完整开发记录；更早历史由 `scripts/agent_changelog_archive.py` 自动归档到 `docs/changelog/`。

默认接手请读 `docs/RECENT_HISTORY.md`。需要追溯具体历史时，再按月份查看归档文件。

## 维护方式

```bash
python3 scripts/agent_changelog_archive.py --keep 10
```

脚本默认保留最近 10 条完整记录，并刷新 `docs/RECENT_HISTORY.md`。

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

## 2026-06-18 23:28 Asia/Shanghai P2-004 实现 OpenAI provider 代码骨架

**任务**：P2-004 实现 OpenAI provider 代码骨架

**改动**：
- 新增 `AiProviderProperties`：`@ConfigurationProperties` 绑定 `lifepilot.ai.provider` 和 `lifepilot.ai.openai.*`
- 新增 `AiProviderConfig`：基于 provider 值条件注入 MockAiProvider 或 OpenAiProvider；openai+空 key 自动回退并 WARN 日志
- 新增 `OpenAiProvider`：通过 RestClient 调用 Chat Completions API，含 JSON 反序列化、code fence 清理、可配置重试和超时
- 修改 `MockAiProvider`：移除 `@Component`，改由 AiProviderConfig 手动创建 Bean
- 更新 `application.yml`：补充完整 openai 子配置（api-key、base-url、model、temperature、max-tokens、timeout-seconds、retry-max-attempts）
- 更新测试 `application.yml`：显式设置 `provider: mock`
- 新增 `OpenAiProviderTest`（8 项）：Transaction/Shopping/Todo 成功路径、code fence 清理、500 错误返回 null、畸形 JSON 返回 null、空 choices 返回 null、null 输入仍调用 API
- 新增 `AiProviderConfigTest`（5 项）：Spring 默认注入 MockAiProvider、openai+空 key 回退 Mock、未知 provider 抛异常、openai+有效 key 创建 OpenAiProvider、mock 创建 MockAiProvider

**验证**：
- 后端 `./mvnw test`：118 tests passed
- 前端 `npm run build`：通过（vue-tsc + vite build）

**建议提交信息**：
```
feat(ai): 实现 OpenAI provider 代码骨架和条件注入

- 新增 AiProviderProperties 绑定 lifepilot.ai.openai.* 配置
- 新增 AiProviderConfig 条件注入：mock/openai 两种 provider
- 新增 OpenAiProvider：RestClient + Chat Completions + JSON 反序列化 + 超时重试
- provider=openai 且 API Key 为空时自动回退 MockAiProvider 并打印警告
- 新增 OpenAiProviderTest（8 项）+ AiProviderConfigTest（5 项），118 tests passed
```

## 2026-06-18 23:02 Asia/Shanghai

- Agent 任务名称：P2-003 增加后端 Service 层单元测试。
- 修改文件：`backend/src/test/java/com/lifepilot/ai/AiServiceTests.java`、`backend/src/test/java/com/lifepilot/statistics/StatisticServiceTests.java`、`docs/BACKLOG.md`、`docs/CURRENT_STATE.md`、`docs/CHANGELOG_AGENT.md`。
- 实现内容：AiServiceTests（Mockito 单元测试，mock AiProvider + HouseholdService + 各 Mapper）覆盖 parseTransaction/parseShoppingList/parseTodo 各三种路径（正常返回、null 返回 needsReview、非成员抛 BusinessException），共 9 项；StatisticServiceTests 覆盖 getOverview 空数据零值/收支计算/低库存检测、getInventoryStats 空数据/分类分组+低库存、getTodoStats 空数据/四种状态计数+逾期判定/已完成不计逾期，共 8 项。
- 测试结果：后端 `./mvnw test` 通过，105 tests passed（原有 88 + 新增 9 AiServiceTests + 8 StatisticServiceTests）；前端 `npm test` 通过（24 项）。
- 遗留问题：P2-004（OpenAI provider 代码骨架）尚未实现。
- 下一步任务：P2-004 实现 OpenAI provider 代码骨架。
- 建议 commit message：`test(backend): 增加 AiService 和 StatisticService 单元测试`

## 2026-06-18 22:53 Asia/Shanghai

- Agent 任务名称：修复 changelog 归档排序并明确决策日志职责。
- 修改文件：`scripts/agent_changelog_archive.py`、`docs/DECISION_LOG.md`、`docs/CHANGELOG_AGENT.md`、`docs/RECENT_HISTORY.md`、`docs/changelog/2026-06.md`。
- 实现内容：归档脚本新增条目时间解析和倒序排序；每次运行都会规范化已有 `docs/changelog/*.md` 归档文件；`docs/changelog/2026-06.md` 重新生成为按时间倒序；Decision Log 顶部补充职责边界，明确只记录长期决策和原因，不记录普通开发流水。
- 测试结果：`python3 scripts/agent_changelog_archive.py` 通过；`python3 -m py_compile scripts/agent_changelog_archive.py scripts/agent_doc_check.py` 通过；`python3 scripts/agent_doc_check.py` 通过；`git diff --check` 通过；后端 `mvn test` 通过，88 tests passed；前端 `npm run build` 通过。
- 遗留问题：无。
- 下一步任务：P2-003 增加后端 Service 层单元测试。
- 建议 commit message：`docs(agent): 完善提交说明和历史归档规则`

## 2026-06-18 22:37 Asia/Shanghai

- Agent 任务名称：完善 Commit Message 详细度规则。
- 修改文件：`docs/AGENT_GIT_RULES.md`、`docs/AGENT_REVIEW_CHECKLIST.md`、`docs/DECISION_LOG.md`、`docs/CHANGELOG_AGENT.md`。
- 实现内容：Git 规则新增按提交复杂度决定 message body 的规范；小提交允许一行，中型提交建议使用 3-5 条中文 bullet，大型提交必须写中文 bullet body；新增中大型提交示例；自审清单增加 commit body 检查项；决策日志记录该规则的原因。
- 测试结果：`python3 scripts/agent_changelog_archive.py` 通过；`python3 scripts/agent_doc_check.py` 通过；`git diff --check` 通过。
- 遗留问题：未自动 amend 既有提交，避免改写最新历史。
- 下一步任务：P2-003 增加后端 Service 层单元测试。
- 建议 commit message：`docs(git): 完善提交信息详细度规则`

## 2026-06-18 22:32 Asia/Shanghai

- Agent 任务名称：新增 P2 任务并执行 P2-002 CI 增加前端测试步骤。
- 修改文件：`.github/workflows/ci.yml`、`docs/BACKLOG.md`、`docs/CURRENT_STATE.md`、`docs/CHANGELOG_AGENT.md`。
- 实现内容：BACKLOG 新增 P2-002（CI 前端测试）、P2-003（后端 Service 层单元测试）、P2-004（OpenAI provider 代码骨架）；ci.yml 前端 job 在 `npm run build` 步骤前新增 `npm test` 步骤，确保前端 Vitest 测试在每次 CI 提交时自动运行。
- 测试结果：前端 `npm test` 通过（24 项）；后端 `./mvnw test` 通过（88 项）。
- 遗留问题：P2-003（后端 Service 层单元测试）和 P2-004（OpenAI provider 代码骨架）尚未实现。
- 下一步任务：P2-003 增加后端 Service 层单元测试。
- 建议 commit message：`ci(frontend): CI 前端 job 增加 npm test 步骤`
