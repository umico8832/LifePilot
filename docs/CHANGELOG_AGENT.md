# Agent Changelog

本文件只保留最近若干条完整开发记录；更早历史由 `scripts/agent_changelog_archive.py` 自动归档到 `docs/changelog/`。

默认接手请读 `docs/RECENT_HISTORY.md`。需要追溯具体历史时，再按月份查看归档文件。

## 维护方式

```bash
python3 scripts/agent_changelog_archive.py --keep 10
```

脚本默认保留最近 10 条完整记录，并刷新 `docs/RECENT_HISTORY.md`。

## 2026-06-18 22:27 Asia/Shanghai

- Agent 任务名称：P2-001 规划真实 AI provider 配置骨架。
- 修改文件：`docs/ARCHITECTURE.md`、`docs/API_DESIGN.md`、`docs/DECISION_LOG.md`、`docs/BACKLOG.md`、`docs/CURRENT_STATE.md`、`docs/CHANGELOG_AGENT.md`。
- 实现内容：ARCHITECTURE.md AI Provider 设计章节大幅扩展——新增 Provider 接口与当前实现（3 个解析方法 + 月报聚合）、Provider 切换策略表（mock/openai）和 Spring 条件注入说明、OpenAI-compatible Provider YAML 配置模板（api-key/base-url/model/temperature/max-tokens/timeout-seconds/retry-max-attempts）、6 条安全边界（API Key 不入代码、Mock 回退、请求超时和重试、输出结构化、日志脱敏、费用控制）、4 项扩展点；API_DESIGN.md 新增 AI Provider 配置环境变量表（8 个变量含默认值和说明）+ Mock 回退机制说明 + 安全规则；DECISION_LOG.md 新增 provider 配置骨架决策条目。
- 测试结果：纯文档任务，跳过后端测试和前端构建验证；`python3 scripts/agent_changelog_archive.py` 通过；`python3 scripts/agent_doc_check.py` 通过；`git diff --check` 通过。
- 遗留问题：P2 backlog 已清空，无更多 todo 任务。
- 下一步任务：待定（可新增 P2 任务继续开发）。
- 建议 commit message：`docs(ai): 规划 OpenAI-compatible provider 配置骨架和安全边界`

## 2026-06-18 22:23 Asia/Shanghai

- Agent 任务名称：P1-017 增加前端关键测试。
- 修改文件：`frontend/package.json`、`frontend/vite.config.ts`、`frontend/src/stores/__tests__/auth.test.ts`、`frontend/src/stores/__tests__/space.test.ts`、`frontend/src/api/__tests__/http.test.ts`、`docs/BACKLOG.md`、`docs/CURRENT_STATE.md`、`docs/CHANGELOG_AGENT.md`、`docs/TESTING.md`。
- 实现内容：接入 Vitest 4 + jsdom + @vue/test-utils 作为前端测试框架；`vite.config.ts` 增加 `test` 配置（environment: jsdom, globals: true）；`package.json` 增加 `test` 和 `test:watch` 脚本；auth store 测试覆盖：初始状态（无 token/无 user）、从 localStorage 恢复 token、login 设置 token 和 user、register 设置 token 和 user、logout 清空状态、loadCurrentUser 正常加载、loadCurrentUser 无 token 跳过、loadCurrentUser 失败自动 logout、loading 标志管理（共 9 项）；space store 测试覆盖：空状态、fetchSpaces 加载并设第一个为 current、fetchSpaces 不覆盖已有 current、fetchSpace 设置 current、createNewSpace 添加并设 current、renameSpace 更新列表和 current、fetchMembers 加载成员、inviteMember 添加成员、clear 重置、setCurrentSpace 设置（共 10 项）；http interceptor 测试覆盖：请求拦截器有 token 添加 Authorization、无 token 不添加、响应拦截器正常通过、响应拦截器错误拒绝、基础配置验证（共 5 项）。
- 测试结果：前端 `npm test` 通过，3 个测试文件共 24 项测试全部通过；前端 `npm run build` 通过（vue-tsc + vite build）。
- 遗留问题：无。
- 下一步任务：P2-001 规划真实 AI provider 配置骨架。
- 建议 commit message：`test(frontend): 接入 Vitest 并为 auth store、space store 和 http interceptor 增加测试`

## 2026-06-18 21:39 Asia/Shanghai

- Agent 任务名称：升级 Agent 文档为近期历史与机器化工作流。
- 修改文件：`AGENTS.md`、`agent-skills/lifepilot-auto-dev/SKILL.md`、`agent-skills/lifepilot-doc-coauthoring/SKILL.md`、`agent-skills/lifepilot-backend-module/SKILL.md`、`agent-skills/lifepilot-api-data-contract/SKILL.md`、`docs/AUTO_DEV_PROTOCOL.md`、`docs/BACKLOG.md`、`docs/CURRENT_STATE.md`、`docs/ROADMAP.md`、`docs/AGENT_GIT_RULES.md`、`docs/AGENT_REVIEW_CHECKLIST.md`、`docs/TESTING.md`、`docs/HANDOFF.md`、`docs/NEXT_CHAT_PROMPT.md`、`docs/DECISION_LOG.md`、`docs/CHANGELOG_AGENT.md`、`docs/RECENT_HISTORY.md`、`docs/changelog/*`、`scripts/agent_changelog_archive.py`、`scripts/agent_doc_check.py`。
- 实现内容：新增近期历史摘要和自动归档脚本；默认接手从完整 changelog 改为读取 `RECENT_HISTORY`；旧历史自动归档到 `docs/changelog/`；Backlog 开头声明机器化任务格式并为待办任务补充验证命令和完成后更新字段；自主开发协议新增半自主提交模式、分层文档更新规则和更明确停止条件；Git 规则同步自动提交边界和文档检查命令；文档一致性脚本开始检查近期历史和归档说明；相关项目 skills 同步当前状态、历史记录和近期历史的职责边界。
- 测试结果：`python3 scripts/agent_changelog_archive.py` 通过；`python3 scripts/agent_doc_check.py` 通过；`git diff --check` 通过。
- 遗留问题：未接入 Git hook；后续框架稳定后再考虑抽成通用模板。
- 下一步任务：P1-017 增加前端关键测试。
- 建议 commit message：`docs(agent): 完善 Agent 文档工作流`

## 2026-06-18 21:06 Asia/Shanghai

- Agent 任务名称：P1-016 前端分类管理 UI 集成。
- 修改文件：`frontend/src/views/finance/FinanceView.vue`、`docs/BACKLOG.md`、`docs/CURRENT_STATE.md`、`docs/CHANGELOG_AGENT.md`。
- 实现内容：FinanceView 脚本新增分类状态（categories、categoriesLoading、categoriesError、categoryDialogVisible、newCategoryForm、filteredCategories）+ 新增 `loadCategories`（空间切换时并行加载）、`getCategoryName`、`openCategoryDialog`、`handleCreateCategory`（含类型参数）、`handleDeleteCategory`（含确认对话框和关联清理）函数 + `watch` 表单 type 变化自动清空不匹配分类 + `onMounted` 和 `handleSpaceChange` 并行加载交易和分类 + 记账表单（创建/编辑）新增分类选择器（el-select 按类型筛选 + clearable + 图标显示 + 空分类提示）+ 记账 payload 适配 `categoryId: null → undefined` 转换 + 交易列表表格新增「分类」列（显示分类 Tag 或「-」）+ 工具栏新增「分类管理」按钮（Tags 图标）+ 分类管理对话框（支出/收入分组列表 + 创建表单含图标输入 + 删除按钮 + 加载/错误态 + el-divider 分隔 + 增加按钮区分支出/收入类型）+ AI 草稿手动编辑预填 categoryId: null + 新增 CSS 类（.no-category、.category-hint、.category-section、.category-section-title、.category-empty、.category-list、.category-item、.category-item-name、.category-loading、.add-category-form、.add-category-input）。
- 测试结果：后端 `./mvnw test` 通过，88 tests passed（含 22 项 AiControllerTests）；前端 `npm run build` 通过（vue-tsc + vite build）。
- 遗留问题：P1-017（前端测试）和 P2-001（AI provider 配置骨架）尚未实现。
- 下一步任务：P1-017 增加前端关键测试。
- 建议 commit message：`feat(finance): 集成前端分类管理 UI`

## 2026-06-18 20:51 Asia/Shanghai

- Agent 任务名称：P1-015 增加更多统计接口。
- 修改文件：`backend/src/main/java/com/lifepilot/statistics/dto/InventoryStatsResponse.java`、`backend/src/main/java/com/lifepilot/statistics/dto/TodoStatsResponse.java`、`backend/src/main/java/com/lifepilot/statistics/StatisticService.java`、`backend/src/main/java/com/lifepilot/statistics/StatisticController.java`、`backend/src/main/java/com/lifepilot/ai/MockAiProvider.java`、`backend/src/test/java/com/lifepilot/ai/AiControllerTests.java`、`frontend/src/api/statistics.ts`、`docs/BACKLOG.md`、`docs/CURRENT_STATE.md`、`docs/CHANGELOG_AGENT.md`。
- 实现内容：InventoryStatsResponse DTO（totalItems、lowStockCount、byCategory 分类统计）+ TodoStatsResponse DTO（按 pending/in_progress/completed/cancelled 分计、overdueCount 逾期数）+ StatisticService 新增 TodoTaskMapper 注入和 `getInventoryStats`/`getTodoStats` 方法 + StatisticController GET `/api/spaces/{spaceId}/statistics/inventory` 和 `/api/spaces/{spaceId}/statistics/todos` + 前端 `statistics.ts` 新增 `InventoryStatsResponse`/`TodoStatsResponse` 类型和 `getInventoryStats`/`getTodoStats` 函数 + 修复 MockAiProvider `PRIORITY_KEYWORDS` 为 `LinkedHashMap` 确保迭代顺序确定性（修复 Map.ofEntries 非确定性迭代导致 parseTodoWithPriorityKeyword 测试间歇性失败）+ 修复测试期望值匹配实际解析行为。
- 测试结果：后端 `./mvnw test` 通过，88 tests passed（含 22 项 AiControllerTests）；前端 `npm run build` 通过（vue-tsc + vite build）。
- 遗留问题：P1-016（前端分类管理 UI）和 P1-017（前端测试）尚未实现。
- 下一步任务：P1-016 前端分类管理 UI 集成。
- 建议 commit message：`feat(statistics): 增加库存和待办统计接口`

## 2026-06-18 20:35 Asia/Shanghai

- Agent 任务名称：P1-014 扩展 AI mock provider：月报草稿。
- 修改文件：`backend/src/main/java/com/lifepilot/ai/dto/MonthlyReportResponse.java`、`backend/src/main/java/com/lifepilot/ai/AiService.java`、`backend/src/main/java/com/lifepilot/ai/AiController.java`、`backend/src/test/java/com/lifepilot/ai/AiControllerTests.java`、`frontend/src/api/ai.ts`、`frontend/src/views/HomeView.vue`、`docs/BACKLOG.md`、`docs/CURRENT_STATE.md`、`docs/CHANGELOG_AGENT.md`。
- 实现内容：MonthlyReportResponse DTO（FinanceSummary 含 topExpenseCategories、InventorySummary、ShoppingSummary、TodoSummary、highlights、suggestions、reportText）+ AiService 新增 TodoTaskMapper/TransactionRecordMapper/TransactionCategoryMapper/InventoryItemMapper/ShoppingListMapper 注入和 `generateMonthlyReport` 方法（按月筛选记账记录、聚合分类支出、计算库存低库存预警数、购物清单数、待办待处理/已完成/逾期数，生成亮点和建议文案，输出完整报告文本）+ AiController GET `/api/ai/spaces/{spaceId}/monthly-report?year=&month=` + 2 项测试（空数据返回和认证校验）+ 前端 `ai.ts` MonthlyReport 系列类型和 `generateMonthlyReport` 函数 + HomeView.vue 新增「生成本月生活报告」按钮 + 月报对话框（亮点列表、财务概览四宫格、支出分类 TOP 5、库存/购物/待办摘要、建议列表、完整报告文本可折叠）。
- 测试结果：后端 `./mvnw test` 通过，88 tests passed（含 22 项 AiControllerTests）；前端 `npm run build` 通过（vue-tsc + vite build）。
- 遗留问题：P1-015（更多统计接口）和 P1-016（前端分类管理 UI）、P1-017（前端测试）尚未实现。
- 下一步任务：P1-015 增加更多统计接口。
- 建议 commit message：`feat(ai): 实现月度生活报告 AI mock 生成`

## 2026-06-18 20:28 Asia/Shanghai

- Agent 任务名称：P1-013 扩展 AI mock provider：待办草稿。
- 修改文件：`backend/src/main/java/com/lifepilot/ai/dto/TodoDraftResponse.java`、`backend/src/main/java/com/lifepilot/ai/dto/ParseTodoRequest.java`、`backend/src/main/java/com/lifepilot/ai/AiProvider.java`、`backend/src/main/java/com/lifepilot/ai/MockAiProvider.java`、`backend/src/main/java/com/lifepilot/ai/AiService.java`、`backend/src/main/java/com/lifepilot/ai/AiController.java`、`backend/src/test/java/com/lifepilot/ai/AiControllerTests.java`、`frontend/src/api/ai.ts`、`frontend/src/views/todo/TodoView.vue`、`docs/BACKLOG.md`、`docs/CURRENT_STATE.md`、`docs/CHANGELOG_AGENT.md`。
- 实现内容：TodoDraftResponse DTO（items 列表含 title/description/priority/dueAt、needsReview、rawInput、validationMessage）+ ParseTodoRequest DTO + AiProvider 接口新增 `parseTodo(String)` 方法 + MockAiProvider 待办解析实现（中文分隔符分割、优先级关键词识别 urgent/high/low、相对截止日期推断 今天/明天/后天/本周/下周/月底、前缀动词清理）+ AiService + AiController POST `/api/ai/spaces/{spaceId}/parse-todo` + 6 项测试（多任务解析、优先级关键词、截止日期、空文本校验、认证校验、低优先级）+ 前端 `ai.ts` TodoDraft/TodoDraftItem 类型和 `parseTodo` 函数 + TodoView.vue AI 助手输入框 + 草稿编辑对话框（可编辑标题/优先级/截止日期、可增删任务、确认后创建真实待办）。
- 测试结果：后端 `./mvnw test` 通过，86 tests passed（含 20 项 AiControllerTests）；前端 `npm run build` 通过（vue-tsc + vite build）。
- 遗留问题：AI 月报草稿（P1-014）尚未实现。
- 下一步任务：P1-014 扩展 AI mock provider：月报草稿。
- 建议 commit message：`feat(ai): 实现待办草稿 AI mock 解析`

## 2026-06-18 20:22 Asia/Shanghai

- Agent 任务名称：P1-012 扩展 AI mock provider：购物清单草稿。
- 修改文件：`backend/src/main/java/com/lifepilot/ai/dto/ShoppingDraftResponse.java`、`backend/src/main/java/com/lifepilot/ai/dto/ParseShoppingRequest.java`、`backend/src/main/java/com/lifepilot/ai/AiProvider.java`、`backend/src/main/java/com/lifepilot/ai/MockAiProvider.java`、`backend/src/main/java/com/lifepilot/ai/AiService.java`、`backend/src/main/java/com/lifepilot/ai/AiController.java`、`backend/src/test/java/com/lifepilot/ai/AiControllerTests.java`、`frontend/src/api/ai.ts`、`frontend/src/views/shopping/ShoppingView.vue`、`docs/BACKLOG.md`、`docs/CURRENT_STATE.md`、`docs/CHANGELOG_AGENT.md`。
- 实现内容：ShoppingDraftResponse DTO（清单名称、预算、物品列表、needsReview、rawInput、validationMessage）+ ParseShoppingRequest DTO + AiProvider 接口新增 `parseShoppingList(String)` 方法 + MockAiProvider 购物清单解析实现（中文分隔符分割、数量+单位+名称正则匹配、上下文关键词推断清单名称、前缀动词清理）+ AiService + AiController POST `/api/ai/spaces/{spaceId}/parse-shopping` + 6 项测试（多物品解析、数量单位识别、上下文清单名、空文本校验、认证校验、单物品 needsReview）+ 前端 `ai.ts` ShoppingDraft/ShoppingDraftItem 类型和 `parseShoppingList` 函数 + ShoppingView.vue AI 助手输入框 + 草稿编辑对话框（清单名称可编辑、物品列表可增删改、确认后创建真实购物清单和物品）。
- 测试结果：后端 `./mvnw test` 通过，80 tests passed（含 14 项 AiControllerTests）；前端 `npm run build` 通过（vue-tsc + vite build）。
- 遗留问题：AI 待办草稿（P1-013）和月报草稿（P1-014）尚未实现。
- 下一步任务：P1-013 扩展 AI mock provider：待办草稿。
- 建议 commit message：`feat(ai): 实现购物清单草稿 AI mock 解析`

## 2026-06-18 16:20 Asia/Shanghai

- Agent 任务名称：简化自主开发文档权威关系。
- 修改文件：`AGENTS.md`、`docs/AUTO_DEV_PROTOCOL.md`、`docs/BACKLOG.md`、`docs/CURRENT_STATE.md`、`docs/HANDOFF.md`、`docs/NEXT_CHAT_PROMPT.md`、`docs/CHANGELOG_AGENT.md`、`docs/DECISION_LOG.md`、`docs/TESTING.md`、`docs/AGENT_REVIEW_CHECKLIST.md`、`scripts/agent_doc_check.py`。
- 实现内容：明确 `CURRENT_STATE` 为唯一当前状态源、`BACKLOG` 为唯一任务源、`CHANGELOG_AGENT` 为历史源；将 `HANDOFF` 收敛为稳定运行手册，将 `NEXT_CHAT_PROMPT` 收敛为极简入口；新增 P1-012～P1-017 和 P2-001 后续任务；新增文档一致性检查脚本。
- 测试结果：`python3 scripts/agent_doc_check.py` 通过；`git diff --check` 通过。
- 遗留问题：无。
- 下一步任务：P1-012 扩展 AI mock provider：购物清单草稿。
- 建议 commit message：`docs(agent): 简化自主开发文档权威关系`

## 2026-06-18 15:41 Asia/Shanghai

- Agent 任务名称：P1-011 实现票据与文件管理 CRUD（Phase 11）。
- 修改文件：`backend/src/main/resources/db/migration/V8__create_document_record.sql`、`backend/src/main/java/com/lifepilot/document/**`（DocumentRecord 实体、DocumentRecordMapper、DTOs、DocumentService、DocumentController）、`backend/src/test/java/com/lifepilot/document/DocumentControllerTests.java`、`frontend/src/api/document.ts`、`frontend/src/views/document/DocumentView.vue`、`frontend/src/router/index.ts`、`frontend/src/layouts/AppShell.vue`、`docs/BACKLOG.md`、`docs/CURRENT_STATE.md`、`docs/CHANGELOG_AGENT.md`、`docs/API_DESIGN.md`。
- 实现内容：Flyway V8 迁移创建 `document_record` 表（含 title、type、issuer、document_date、expire_at、storage_location、metadata_json 字段）；后端 `document` 模块实现文档 CRUD（POST/GET/PATCH/DELETE `/api/spaces/{spaceId}/documents`），类型校验（invoice/receipt/warranty/contract/manual/certificate/other），支持按类型筛选（`?type=invoice`），`DocumentResponse` 自动计算 `expiringSoon` 布尔值（30 天内到期标识）；前端新增 `document.ts` API 客户端、DocumentView.vue（空间选择器 + 类型筛选 + 摘要栏（总数 + 即将过期数）+ 文档列表表格（类型标签、标题、签发方、文档日期、到期日、存放位置）+ 创建/编辑对话框 + 空态/错误态）、路由 `/document`、AppShell 导航新增「文档」入口（FileText 图标）。
- 测试结果：后端 `./mvnw test` 通过，74 tests passed（含 7 项 DocumentControllerTests：创建+列表、按 ID 查询、更新+删除、按类型筛选、非成员权限校验、认证校验、无效类型校验）；前端 `npm run build` 通过（vue-tsc + vite build）；Flyway V1-V8 在 H2 测试数据库迁移通过。
- 遗留问题：AI 其他端点（create-shopping-list-draft、create-todo-draft、monthly-report-draft）尚未实现；更多统计接口（分类统计、库存统计、待办统计）尚未实现。
- 下一步任务：P1-012 待定。
- 建议 commit message：`feat(document): 实现票据与文件管理 CRUD`

## 2026-06-18 14:27 Asia/Shanghai

- Agent 任务名称：P1-010 实现菜谱管理 CRUD（Phase 8）。
- 修改文件：`backend/src/main/resources/db/migration/V7__create_recipe.sql`、`backend/src/main/java/com/lifepilot/recipe/**`（Recipe 实体、RecipeMapper、DTOs、RecipeService、RecipeController）、`backend/src/test/java/com/lifepilot/recipe/RecipeControllerTests.java`、`frontend/src/api/recipe.ts`、`frontend/src/views/recipe/RecipeView.vue`、`frontend/src/router/index.ts`、`frontend/src/layouts/AppShell.vue`、`docs/BACKLOG.md`、`docs/CURRENT_STATE.md`、`docs/CHANGELOG_AGENT.md`、`docs/API_DESIGN.md`。
- 实现内容：Flyway V7 迁移创建 `recipe` 表（含 household_id、name、description、ingredients_json、steps_json、created_by 字段）；后端 `recipe` 模块实现菜谱 CRUD（POST/GET/PATCH/DELETE `/api/spaces/{spaceId}/recipes`），所有接口需要空间成员权限；前端新增 `recipe.ts` API 客户端、RecipeView.vue（空间选择器 + 菜谱列表表格（名称、描述、食材解析显示、创建日期）+ 创建/编辑对话框（名称、描述、食材 JSON、步骤 JSON）+ 空态/错误态）、路由 `/recipe`、AppShell 导航新增「菜谱」入口（ChefHat 图标）。
- 测试结果：后端 `./mvnw test` 通过，67 tests passed（含 7 项 RecipeControllerTests：创建+列表、按 ID 查询、更新+删除、非成员权限校验、认证校验、创建校验、404 校验）；前端 `npm run build` 通过（vue-tsc + vite build）；Flyway V1-V7 在 H2 测试数据库迁移通过。
- 遗留问题：AI 其他端点尚未实现；Phase 11（票据与文件管理）尚未实现。
- 下一步任务：P1-011 待定。
- 建议 commit message：`feat(recipe): 实现菜谱管理 CRUD`
