# Current State

## 当前阶段

P9-004 已按本机环境限制完成替代验证；由于 Docker Desktop 处于手动暂停状态、3307 MySQL 只读探测超时且项目未安装 Playwright，真实 demo seed 写入和浏览器冒烟未能执行，已记录补跑命令。当前进入 P10 跨模块权限矩阵落地阶段（Phase 25/26）。

## 当前最高优先级任务

P10-001 购物清单 viewer 写保护加固。

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
- P6-002 根据饮食计划和库存生成购物清单草稿：后端 `AiProvider` 新增 `draftShoppingListFromMealPlan` 方法；`MockAiProvider` 解析菜谱食材数量/单位并结合库存生成缺口购物草稿；`OpenAiProvider` 委托本地 mock 算法；`AiService` 按日期范围查询饮食计划、对应菜谱和库存；`AiController` 新增 `GET /api/ai/spaces/{spaceId}/meal-plan-shopping-draft` 端点；前端 `ai.ts` 新增 API 方法，`MealPlanView.vue` 新增“生成采购清单”按钮、草稿面板和确认创建购物清单流程；新增 `MockAiProviderTest` 和 AiService/API 测试覆盖。
- P7-001 AI 调用日志持久化与查询：新增 `V10__create_ai_call_log.sql`、`AiCallLog`、`AiCallLogMapper`、`AiCallLogService` 和 `AiCallLogResponse`；`AiService` 为 AI 解析、月报、菜谱推荐和饮食计划采购草稿记录成功/失败日志，保存 provider、scenario、prompt hash、脱敏摘要、状态、耗时和错误摘要；`AiController` 新增 `GET /api/ai/spaces/{spaceId}/call-logs` 查询接口；前端 `ai.ts` 新增 `AiCallLog` 类型和 `listAiCallLogs()` 方法；新增日志服务、AI Service、控制器和前端 API 测试。
- P8-001 前端 AI 调用日志审计页：新增 `AiLogView.vue` 和 `/ai-logs` 路由；AppShell 导航新增“AI 日志”入口；页面按空间读取 `listAiCallLogs()`，支持场景、状态和条数筛选，展示调用状态、耗时、脱敏请求/响应摘要、prompt hash 和错误摘要；新增 `AiLogView.test.ts` 覆盖加载、无空间、空态和错误态。
- P8-002 AI 调用日志统计摘要接口：新增 `AiCallLogSummaryResponse` DTO、`AiCallLogService.summarizeLogs`、`AiService.summarizeCallLogs` 和 `GET /api/ai/spaces/{spaceId}/call-logs/summary?days=` 端点；前端 `ai.ts` 新增 `AiCallLogSummary` 类型和 `getAiCallLogSummary()`；测试覆盖统计计算、空数据、权限委托、控制器端点和前端 API 参数。
- P8-003 演示数据与本地体验种子脚本：新增 `scripts/demo_seed.sql` 和 `scripts/demo_seed.sh`，默认 dry-run，显式 `--apply` 后重置并创建 `demo@lifepilot.local` demo 用户、家庭空间和记账、购物、库存、待办、菜谱、饮食计划、票据文档、AI 日志数据；README 和 HANDOFF 记录默认账号、使用方式、幂等清理说明和验证命令。
- P8-004 家庭成员管理体验完善：后端新增 `DELETE /api/spaces/{spaceId}/members/{memberId}`，成员角色更新和移除加入角色白名单、owner/admin 权限和至少保留一名 owner/admin 保护；inactive 成员可通过添加流程重新激活；前端空间页新增管理员角色下拉、移除成员操作、普通成员只读视图；API/store 测试和控制器测试覆盖管理路径。
- P9-001 家庭空间邀请链接基础能力：新增 `household_invitation` 表、邀请实体/Mapper/DTO 和 `HouseholdInvitationService`；后端提供创建、列表、撤销和接受邀请接口，token 只以 SHA-256 hash 落库，创建响应一次性返回明文 token；前端 `space.ts` 新增邀请 API 类型和方法；后端控制器测试覆盖管理员创建、普通成员/非成员禁止、过期/撤销/重复接受、邮箱不匹配、非法角色和接受后成为成员。
- P9-002 前端邀请管理与接受邀请体验：空间页管理员视图新增邀请链接列表、生成邀请弹窗、复制链接和撤销操作；普通成员保持只读且不显示邀请管理入口；新增 `/spaces/invitations/accept?token=` 接受邀请页，支持 URL token 或手动输入，成功后刷新空间列表并跳转空间页；新增 store 和页面测试覆盖创建、撤销、接受和只读状态。
- P9-003 角色权限体验和测试矩阵加固：`docs/API_DESIGN.md` 和 `docs/ARCHITECTURE.md` 明确 owner/admin/member/viewer 在空间、成员、邀请、记账、购物、库存、待办、菜谱、饮食计划、票据和 AI 日志中的读写边界；后端记账写操作改为 `owner/admin/member` 可写、`viewer` 只读；FinanceView 根据角色隐藏写操作并显示只读提示；补充 controller/service 和页面测试覆盖 viewer/member/admin 路径。
- P9-004 Demo seed 真实 MySQL 冒烟验证与浏览器检查：完成脚本语法和 dry-run 验证，确认 mysql 客户端存在；Docker Desktop 手动暂停、`MYSQL_PORT=3307` 只读连接探测 10 秒超时、项目未安装 Playwright，因此未执行真实 `--apply`/`--verify` 和浏览器冒烟；已补充 Phase 25/26 与 P10-001～P10-004 任务池，避免 backlog 清空。

## 当前阻塞项

无。

## 下一项自动任务

P10-001 购物清单 viewer 写保护加固：按 P9-003 角色矩阵补齐购物清单后端写权限和前端只读体验，确保 viewer 只能查看购物清单和清单项。

## 最近验证结果

- 工程体检（2026-06-25 14:25 Asia/Shanghai）：GitHub Actions 最近远端 CI #12（`14ebfb1`）成功；上一失败 #11 为 Backend Tests / Run tests，后续 `14ebfb1` 已修复 `MealPlanMapper` 注册问题。
- 本次本地验证（P7-001）：`cd backend && ./mvnw test -B -Dtest="AiServiceTests,AiCallLogServiceTests,AiControllerTests"` 通过，45 tests passed；`cd frontend && npm test -- ai.test.ts` 通过，7 tests passed；`cd backend && ./mvnw test -B` 通过，247 tests passed；`cd frontend && npm test` 通过，11 个测试文件 93 tests passed；`cd frontend && npm run build` 通过，仍有既有第三方 `@vueuse/core` Rolldown pure annotation 警告；`python3 scripts/agent_changelog_archive.py` 通过；`python3 scripts/agent_doc_check.py` 按预期返回“BACKLOG has no todo tasks”，触发当前阶段完成的停止条件；`git diff --check` 通过。
- 后端 `./mvnw test -B`：通过，247 tests passed（P7-001 后新增 7 项测试）。
- 前端 `npm test`：通过，11 个测试文件共 93 项测试全部通过。
- 前端 `npm run build`：通过；仍有既有第三方 `@vueuse/core` Rolldown pure annotation 警告。
- 实机联调：MySQL（3307）下后端服务成功启动（18081），健康检查返回 `UP`；浏览器完成注册、自动登录和创建家庭空间，未发现 console error。
- 文档归档脚本 `python3 scripts/agent_changelog_archive.py`：通过。
- 文档一致性检查 `python3 scripts/agent_doc_check.py`：任务池无 `todo`，按自主开发协议返回当前阶段完成的停止条件。
- `git diff --check`：通过。
- 文档规划（2026-06-28 19:52 Asia/Shanghai）：新增 P8-001～P8-004 任务池和 Phase 21/22 路线；`python3 scripts/agent_changelog_archive.py` 通过；`python3 scripts/agent_doc_check.py` 通过；`git diff --check` 通过。
- P8-001 本地验证（2026-06-28 19:58 Asia/Shanghai）：`cd frontend && npm test -- AiLogView.test.ts` 通过，1 个测试文件 4 tests passed；`cd frontend && npm test` 通过，12 个测试文件 97 tests passed；`cd frontend && npm run build` 通过，仍有既有第三方 `@vueuse/core` Rolldown pure annotation 警告；浏览器自动化尝试因项目未安装 Playwright 包且本轮未暴露 in-app browser 工具而未完成，已由组件测试覆盖主要 UI 状态。
- P8-002 本地验证（2026-06-28 20:04 Asia/Shanghai）：`cd backend && ./mvnw test -B -Dtest="AiCallLogServiceTests,AiServiceTests,AiControllerTests"` 通过，50 tests passed；`cd frontend && npm test -- ai.test.ts` 通过，1 个测试文件 8 tests passed；`cd backend && ./mvnw test -B` 通过，252 tests passed；`cd frontend && npm test` 通过，12 个测试文件 98 tests passed；`cd frontend && npm run build` 通过，仍有既有第三方 `@vueuse/core` Rolldown pure annotation 警告；`python3 scripts/agent_changelog_archive.py` 通过；`python3 scripts/agent_doc_check.py` 通过；`git diff --check` 通过。
- P8-003 本地验证（2026-06-28 21:10 Asia/Shanghai）：`bash -n scripts/demo_seed.sh` 通过；`scripts/demo_seed.sh --dry-run` 通过；`cd backend && ./mvnw test -B` 通过，252 tests passed；`cd frontend && npm run build` 通过，仍有既有第三方 `@vueuse/core` Rolldown pure annotation 警告；真实 `--apply` 未运行，因为 Docker Desktop 处于手动暂停状态，无法启动 MySQL，且本地 `3306` 未监听、`3307` 探测无响应后已中断；后续需在 MySQL 可用后补跑 `MYSQL_PORT=3307 scripts/demo_seed.sh --apply && MYSQL_PORT=3307 scripts/demo_seed.sh --verify`。
- P8-004 本地验证（2026-06-28 21:18 Asia/Shanghai）：`cd backend && ./mvnw test -B -Dtest=HouseholdControllerTests` 通过，13 tests passed；`cd frontend && npm test -- space.test.ts` 通过，2 个测试文件 15 tests passed；`cd frontend && npm run build` 通过，仍有既有第三方 `@vueuse/core` Rolldown pure annotation 警告；`cd backend && ./mvnw test -B` 通过，256 tests passed；`cd frontend && npm test` 通过，13 个测试文件 103 tests passed；`python3 scripts/agent_changelog_archive.py` 通过；`git diff --check` 通过；`python3 scripts/agent_doc_check.py` 按预期返回 “BACKLOG has no todo tasks”，触发自主开发停止条件。
- 下一阶段规划（2026-06-28 21:35 Asia/Shanghai）：新增 Phase 23/24 和 P9-001～P9-004 任务池；待运行文档归档和一致性检查。
- P9-001 本地验证（2026-06-28 23:18 Asia/Shanghai）：`cd backend && ./mvnw test -B -Dtest=HouseholdControllerTests` 通过，19 tests passed；`cd frontend && npm test -- space.test.ts` 通过，2 个测试文件 19 tests passed；`cd backend && ./mvnw test -B` 通过，262 tests passed；`cd frontend && npm test` 通过，13 个测试文件 107 tests passed；`cd frontend && npm run build` 通过，仍有既有第三方 `@vueuse/core` Rolldown pure annotation 警告。
- P9-001 文档验证（2026-06-28 23:18 Asia/Shanghai）：`python3 scripts/agent_changelog_archive.py` 通过，保留 10 条并刷新 `docs/RECENT_HISTORY.md`；`python3 scripts/agent_doc_check.py` 通过，确认下一任务 P9-002 与 BACKLOG 一致；`git diff --check` 通过。
- P9-002 本地验证（2026-06-28 23:25 Asia/Shanghai）：`cd frontend && npm test -- space.test.ts SpaceView.test.ts AcceptInvitationView.test.ts` 通过，4 个测试文件 27 tests passed；`cd frontend && npm run build` 通过，仍有既有第三方 `@vueuse/core` Rolldown pure annotation 警告；`cd frontend && npm test` 通过，15 个测试文件 115 tests passed；浏览器冒烟未运行，因为 Docker Desktop 处于手动暂停状态且项目未安装 Playwright 包，已由 API/store/页面组件测试覆盖主要交互状态。
- P9-002 文档验证（2026-06-28 23:25 Asia/Shanghai）：`python3 scripts/agent_changelog_archive.py` 通过，保留 10 条并刷新 `docs/RECENT_HISTORY.md`；`python3 scripts/agent_doc_check.py` 通过，确认下一任务 P9-003 与 BACKLOG 一致；`git diff --check` 通过。
- P9-003 本地验证（2026-06-28 23:31 Asia/Shanghai）：`cd backend && ./mvnw test -B -Dtest=TransactionControllerTests,HouseholdControllerTests` 通过，27 tests passed；`cd frontend && npm test -- FinanceView.test.ts SpaceView.test.ts space.test.ts` 通过，4 个测试文件 38 tests passed；首次 `cd backend && ./mvnw test -B` 因旧 `TransactionServiceTests` 仍断言 `requireSpaceMembership` 失败，已修正为 `requireSpaceRole` 后重跑通过，265 tests passed；`cd frontend && npm test` 通过，15 个测试文件 116 tests passed；`cd frontend && npm run build` 通过，仍有既有第三方 `@vueuse/core` Rolldown pure annotation 警告。
- P9-003 文档验证（2026-06-28 23:31 Asia/Shanghai）：`python3 scripts/agent_changelog_archive.py` 通过，保留 10 条并刷新 `docs/RECENT_HISTORY.md`；`python3 scripts/agent_doc_check.py` 通过，确认下一任务 P9-004 与 BACKLOG 一致；`git diff --check` 通过。
- P9-004 替代验证（2026-06-28 23:33 Asia/Shanghai）：`bash -n scripts/demo_seed.sh` 通过；`scripts/demo_seed.sh --dry-run` 通过；`docker compose ps --format json` 未通过，Docker Desktop 手动暂停；`command -v mysql && mysql --version` 通过，mysql client 为 8.0.43；`nc -z 127.0.0.1 3306` 返回未监听，`nc -z 127.0.0.1 3307` 返回端口开放；`MYSQL_PORT=3307 perl -e 'alarm 10; exec @ARGV' scripts/demo_seed.sh --verify` 与直接 `mysql SELECT 1` 均在 10 秒后退出 142，未确认可用 MySQL 会话；`test -d frontend/node_modules/playwright` 返回 `no-playwright`。未执行真实写入；后续补跑命令：`MYSQL_PORT=3307 scripts/demo_seed.sh --apply && MYSQL_PORT=3307 scripts/demo_seed.sh --verify`，再启动前后端并检查首页、购物清单、库存提醒、饮食计划和 AI 日志页面。
- P9-004 文档验证（2026-06-28 23:33 Asia/Shanghai）：`python3 scripts/agent_changelog_archive.py` 通过，保留 10 条并刷新 `docs/RECENT_HISTORY.md`；`python3 scripts/agent_doc_check.py` 通过，确认下一任务 P10-001 与 BACKLOG 一致；`git diff --check` 通过。

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
- `AiService` 构造函数新增 `MealPlanMapper` 依赖。
- `AiProvider` 接口新增 `draftShoppingListFromMealPlan` 方法；真实 OpenAI provider 当前仍委托本地 mock 算法，不新增外部 AI 调用。
- `MealPlanMapper` 已显式标注 `@Mapper`，完整测试与真实 MySQL 服务启动均已恢复。
- `ai_call_log` 由 V10 迁移创建，记录 AI 调用审计摘要；自然语言输入只保存 SHA-256 hash 和长度摘要，不保存原文。
- AI 调用日志查询接口为 `GET /api/ai/spaces/{spaceId}/call-logs?scenario=&status=&limit=`，默认 50 条，上限 100 条，需空间成员权限。
- 家庭邀请表 `household_invitation` 由 V11 迁移创建，token 只保存 SHA-256 hash；创建邀请响应会一次性返回明文 token，列表和接受响应不返回明文 token。
- 家庭邀请接口为 `POST/GET/DELETE /api/spaces/{spaceId}/invitations` 和 `POST /api/spaces/invitations/accept`；创建、列表、撤销要求 `owner` 或 `admin`，接受邀请要求登录用户且目标邮箱匹配。
- 前端接受邀请路由为 `/spaces/invitations/accept?token=`，需要登录；空间页的邀请管理仅在家庭空间 `owner/admin` 视图展示。
- 当前角色矩阵约定：`viewer` 只读，`member` 可写普通业务数据，`admin/owner` 可管理空间成员和邀请；记账模块已经在后端和前端完成 viewer 写保护，其余业务模块按该矩阵作为后续加固标准。
- Demo seed 真实写入仍待 MySQL 可用后补跑；本机当前 Docker Desktop 手动暂停，`3307` 端口开放但 MySQL 会话探测超时。
