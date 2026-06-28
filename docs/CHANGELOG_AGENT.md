# Agent Changelog

本文件只保留最近若干条完整开发记录；更早历史由 `scripts/agent_changelog_archive.py` 自动归档到 `docs/changelog/`。

默认接手请读 `docs/RECENT_HISTORY.md`。需要追溯具体历史时，再按月份查看归档文件。

## 维护方式

```bash
python3 scripts/agent_changelog_archive.py --keep 10
```

脚本默认保留最近 10 条完整记录，并刷新 `docs/RECENT_HISTORY.md`。

## 2026-06-28 23:08 Asia/Shanghai 修正任务池耗尽与时间戳规则

- 任务：修正自主开发协议，避免 backlog 清空后长期开发断线，并修正当前时间戳不能凭估算写入的问题。
- 改动：
  - `docs/AUTO_DEV_PROTOCOL.md` 新增“任务池耗尽处理”：backlog 无 `todo` 时必须先检查产品规划并补充下一阶段任务池，只有无法安全规划时才停止。
  - `docs/AUTO_DEV_PROTOCOL.md` 新增“时间戳规则”：写入 changelog、current state、recent history、归档或交接文档时间前，必须先运行系统 `date` 命令确认。
  - `AGENTS.md` 协作原则补充同一约束，提醒新 Agent 不要直接停在无下一任务状态。
  - `docs/AGENT_REVIEW_CHECKLIST.md` 新增时间戳自审项；`docs/DECISION_LOG.md` 记录长期流程决策和时间戳决策。
**验证**：
  - `date '+%Y-%m-%d %H:%M:%S %Z %z'`：确认当前系统时间为 `2026-06-28 23:08:03 CST +0800`。
  - `python3 scripts/agent_changelog_archive.py`：通过。
  - `python3 scripts/agent_doc_check.py`：通过，确认 BACKLOG 存在 P9-001～P9-004 todo，且 `CURRENT_STATE` 下一任务与 P9-001 一致。
  - `git diff --check`：通过。

## 2026-06-28 21:35 Asia/Shanghai 规划 P9 家庭邀请与协作闭环任务池

- 任务：补齐 P8 完成后的下一阶段长期开发入口，避免 backlog 清空导致新 Agent 无可执行任务。
- 改动：
  - `docs/ROADMAP.md` 新增 Phase 23 家庭邀请与协作闭环、Phase 24 协作权限与演示质量加固。
  - `docs/BACKLOG.md` 新增 P9-001～P9-004：家庭空间邀请链接基础能力、前端邀请管理与接受邀请体验、角色权限体验和测试矩阵加固、Demo seed 真实 MySQL 冒烟验证与浏览器检查。
  - `docs/CURRENT_STATE.md` 将当前最高优先级任务更新为 P9-001，并恢复下一项自动任务说明。
**验证**：
  - `python3 scripts/agent_changelog_archive.py`：通过。
  - `python3 scripts/agent_doc_check.py`：通过，确认 BACKLOG 存在 P9-001～P9-004 todo，且 `CURRENT_STATE` 下一任务与 P9-001 一致。
  - `git diff --check`：通过。

## 2026-06-28 21:16 Asia/Shanghai P8-004 家庭成员管理体验完善

- 任务：P8-004 家庭成员管理体验完善
- 改动：
  - 后端：`HouseholdController` 新增 `DELETE /api/spaces/{spaceId}/members/{memberId}`；`HouseholdService` 为成员添加、角色更新和移除补充角色白名单、owner/admin 管理权限、inactive 成员重新激活和至少保留一名 owner/admin 的保护。
  - 前端：`space.ts` 新增 `removeMember()`；space store 新增 `changeMemberRole()` 和 `deleteMember()`；`SpaceView.vue` 在管理员视图中展示角色下拉与移除按钮，普通成员保持只读。
  - 测试：`HouseholdControllerTests` 覆盖管理员更新/移除、普通成员禁止、非成员禁止和最后管理员保护；新增 `frontend/src/api/__tests__/space.test.ts` 并扩展 space store 测试。
  - 文档：`docs/API_DESIGN.md` 记录成员移除接口和权限规则；`docs/ARCHITECTURE.md` 补充成员管理权限边界。
**验证**：
  - `cd backend && ./mvnw test -B -Dtest=HouseholdControllerTests`：13 tests passed。
  - `cd frontend && npm test -- space.test.ts`：2 个测试文件 15 tests passed。
  - `cd frontend && npm run build`：通过；仍有既有第三方 `@vueuse/core` Rolldown pure annotation 警告。
  - `cd backend && ./mvnw test -B`：256 tests passed。
  - `cd frontend && npm test`：13 个测试文件 103 tests passed。
  - `python3 scripts/agent_changelog_archive.py`：通过。
  - `git diff --check`：通过。
  - `python3 scripts/agent_doc_check.py`：按预期返回 “BACKLOG has no todo tasks”，触发自主开发停止条件。

## 2026-06-28 21:10 Asia/Shanghai P8-003 演示数据与本地体验种子脚本

- 任务：P8-003 演示数据与本地体验种子脚本
- 改动：
  - 脚本：新增 `scripts/demo_seed.sql`，在事务中重置并重建 `demo@lifepilot.local` 拥有的 demo 用户、家庭空间、成员关系、记账分类与流水、购物清单与预算、库存临期/低库存项、待办状态、菜谱、饮食计划、票据文档和 AI 调用日志。
  - 命令：新增 `scripts/demo_seed.sh`，支持默认 `--dry-run`、显式 `--apply` 和 `--verify`；通过 `MYSQL_HOST`、`MYSQL_PORT`、`MYSQL_DATABASE`、`MYSQL_USER`、`MYSQL_PASSWORD` 指向本地开发库。
  - 幂等性：重复 `--apply` 会先删除该 demo 用户拥有的空间和业务数据，再插入固定演示数据；不包含真实密钥、个人数据或外部服务调用。
  - 文档：README 和 HANDOFF 记录默认演示账号、使用方式、端口变量、清理/重置注意事项和覆盖页面。
**验证**：
  - `bash -n scripts/demo_seed.sh`：通过。
  - `scripts/demo_seed.sh --dry-run`：通过，确认默认目标和无副作用模式。
  - `cd backend && ./mvnw test -B`：252 tests passed。
  - `cd frontend && npm run build`：通过；仍有既有第三方 `@vueuse/core` Rolldown pure annotation 警告。
  - `MYSQL_PORT=3307 docker compose up -d mysql`：未通过；Docker Desktop 处于手动暂停状态，无法拉起 MySQL。
  - `MYSQL_PWD=lifepilot_dev_password mysql --host=127.0.0.1 --port=3306 --user=lifepilot --database=lifepilot --execute='SELECT 1 AS ok;'`：未通过，3306 未监听。
  - `MYSQL_PWD=lifepilot_dev_password mysql --host=127.0.0.1 --port=3307 --user=lifepilot --database=lifepilot --execute='SELECT 1 AS ok;'`：无响应后中断；未对数据库写入。
  - 待补跑：MySQL 可用后执行 `MYSQL_PORT=3307 scripts/demo_seed.sh --apply && MYSQL_PORT=3307 scripts/demo_seed.sh --verify`。

## 2026-06-28 20:03 Asia/Shanghai P8-002 AI 调用日志统计摘要接口

- 任务：P8-002 AI 调用日志统计摘要接口
- 改动：
  - 后端：新增 `AiCallLogSummaryResponse` DTO；`AiCallLogService.summarizeLogs()` 按空间和最近天数统计总调用数、成功数、失败数、成功率、平均耗时、场景分布和状态分布；`AiService.summarizeCallLogs()` 复用空间成员权限校验；`AiController` 新增 `GET /api/ai/spaces/{spaceId}/call-logs/summary?days=`。
  - 前端：`frontend/src/api/ai.ts` 新增 `AiCallLogSummary`、分组类型和 `getAiCallLogSummary()` 方法。
  - 测试：扩展 `AiCallLogServiceTests`、`AiServiceTests`、`AiControllerTests` 和 `ai.test.ts` 覆盖统计计算、空数据、权限委托、控制器端点、认证要求和前端 API 参数。
  - 文档：`docs/API_DESIGN.md` 记录日志摘要接口与响应字段；`docs/BACKLOG.md` 标记 P8-002 完成；`docs/CURRENT_STATE.md` 指向 P8-003。
**验证**：
  - `cd backend && ./mvnw test -B -Dtest="AiCallLogServiceTests,AiServiceTests,AiControllerTests"`：50 tests passed。
  - `cd frontend && npm test -- ai.test.ts`：1 个测试文件 8 tests passed。
  - `cd backend && ./mvnw test -B`：252 tests passed。
  - `cd frontend && npm test`：12 个测试文件 98 tests passed。
  - `cd frontend && npm run build`：通过；仍有既有第三方 `@vueuse/core` Rolldown pure annotation 警告。
  - `python3 scripts/agent_changelog_archive.py`：通过。
  - `python3 scripts/agent_doc_check.py`：通过。
  - `git diff --check`：通过。

---

## 2026-06-28 19:58 Asia/Shanghai P8-001 前端 AI 调用日志审计页

- 任务：P8-001 前端 AI 调用日志审计页
- 改动：
  - 前端：新增 `frontend/src/views/ai/AiLogView.vue`，通过 `listAiCallLogs()` 读取当前空间 AI 调用日志；支持空间切换、场景筛选、状态筛选和 limit 查询；展示调用场景、provider、状态、耗时、脱敏请求/响应摘要、prompt hash 和错误摘要；包含无空间、加载中、空数据和加载失败状态。
  - 导航：`frontend/src/router/index.ts` 新增 `/ai-logs` 受保护路由；`AppShell.vue` 新增“AI 日志”导航入口。
  - 测试：新增 `frontend/src/views/__tests__/AiLogView.test.ts`，覆盖选中空间加载日志、无空间不加载、空态和错误态。
  - 文档：`docs/BACKLOG.md` 标记 P8-001 完成；`docs/CURRENT_STATE.md` 指向 P8-002；`docs/API_DESIGN.md` 记录前端 `/ai-logs` 已接入日志查询接口。
**验证**：
  - `cd frontend && npm test -- AiLogView.test.ts`：1 个测试文件 4 tests passed。
  - `cd frontend && npm test`：12 个测试文件 97 tests passed。
  - `cd frontend && npm run build`：通过；仍有既有第三方 `@vueuse/core` Rolldown pure annotation 警告。
  - 浏览器自动化：尝试用 Playwright 检查 `/ai-logs`，但项目未安装 `playwright` 包且本轮未暴露 in-app browser 控制工具；未完成浏览器截图检查，主要 UI 状态已由组件测试覆盖。

---

## 2026-06-28 19:52 Asia/Shanghai 规划 P8 可观测性与演示体验任务池

- 任务：补齐上一个阶段完成后的下一批可执行 backlog，恢复自主开发入口。
- 改动：
  - `docs/BACKLOG.md` 新增 P8-001～P8-004：前端 AI 调用日志审计页、AI 调用日志统计摘要接口、演示数据与本地体验种子脚本、家庭成员管理体验完善。
  - `docs/CURRENT_STATE.md` 将当前阶段更新为 P8 可观测性与演示体验阶段，明确最高优先级任务和下一项自动任务为 P8-001。
  - `docs/ROADMAP.md` 新增 Phase 21 可观测性与演示体验、Phase 22 家庭协作体验完善。
**验证**：
  - `python3 scripts/agent_changelog_archive.py`：通过，刷新 `docs/RECENT_HISTORY.md`。
  - `python3 scripts/agent_doc_check.py`：通过，确认 BACKLOG 存在 P8-001～P8-004 todo，且 `CURRENT_STATE` 下一任务与 P8-001 一致。
  - `git diff --check`：通过。

---

## 2026-06-27 14:42 Asia/Shanghai P7-001 AI 调用日志持久化与查询

- 任务：P7-001 AI 调用日志持久化与查询
- 改动：
  - 后端：新增 `V10__create_ai_call_log.sql`，创建 `ai_call_log` 表和按 user/household/scenario/status 的查询索引；新增 `AiCallLog`、`AiCallLogMapper`、`AiCallLogService` 和 `AiCallLogResponse`；`AiService` 为自然语言记账、购物草稿、待办草稿、月报、菜谱推荐和饮食计划采购草稿记录成功日志，并在 provider 异常时记录失败日志后继续抛出；`AiController` 新增 `GET /api/ai/spaces/{spaceId}/call-logs?scenario=&status=&limit=` 查询接口。
  - 安全边界：自然语言输入不保存原文，只保存 SHA-256 `promptHash` 和输入长度等摘要；响应日志只保存类型、条目数、是否需要复核、是否有校验消息等摘要。
  - 前端：`frontend/src/api/ai.ts` 新增 `AiCallLog`、`AiCallLogQuery` 类型和 `listAiCallLogs()` 方法；`ai.test.ts` 覆盖日志查询路径和参数。
  - 测试：新增 `AiCallLogServiceTests`；扩展 `AiServiceTests` 和 `AiControllerTests` 覆盖成功日志、失败日志、日志查询和脱敏摘要。
**验证**：
  - `cd backend && ./mvnw test -B -Dtest="AiServiceTests,AiCallLogServiceTests,AiControllerTests"`：45 tests passed。
  - `cd frontend && npm test -- ai.test.ts`：1 个测试文件 7 tests passed。
  - `cd backend && ./mvnw test -B`：247 tests passed。
  - `cd frontend && npm test`：11 个测试文件 93 tests passed。
  - `cd frontend && npm run build`：通过；仍有既有第三方 `@vueuse/core` Rolldown pure annotation 警告。
  - `python3 scripts/agent_changelog_archive.py`：通过。
  - `python3 scripts/agent_doc_check.py`：按预期返回“BACKLOG has no todo tasks”，触发当前阶段完成的停止条件。
  - `git diff --check`：通过。
- **文档更新**：`docs/BACKLOG.md`、`docs/CURRENT_STATE.md`、`docs/API_DESIGN.md`、`docs/DB_DESIGN.md`、`docs/ARCHITECTURE.md`、`docs/ROADMAP.md`、`docs/CHANGELOG_AGENT.md`。

---

## 2026-06-25 14:25 Asia/Shanghai 工程体检与构建稳定性修复

- 任务：按用户要求对当前项目做完整工程体检，检查 CI、本地安装、测试、构建和文档运行方式。
- 改动：
  - 新增 `REPORT.md`，记录技术栈、CI 失败定位、本地验证结果、已修复问题、仍存在问题和运行/测试/构建方式。
  - 前端 `vite.config.ts` 新增 `build.chunkSizeWarningLimit: 1000`，匹配当前 Element Plus + ECharts 应用体量，消除既有大 chunk 阈值噪音。
  - 更新 `docs/CURRENT_STATE.md` 最近验证结果，补充本次工程体检结论。
- CI 检查：
  - GitHub Actions 最近远端 CI #12（`14ebfb1`）成功。
  - 上一失败 CI #11（`ddf0408`）失败 job 为 Backend Tests，失败步骤为 Run tests；公共 API 无权限下载完整 job logs（403），后续提交 `14ebfb1` 已修复 `MealPlanMapper` 注册问题。
**验证**：
  - `cd backend && ./mvnw test -B`：240 tests passed。
  - `cd frontend && npm ci`：通过。
  - `cd frontend && npm test`：11 个测试文件 92 tests passed。
  - `cd frontend && npm run build`：通过；大 chunk 警告已消除，仍有第三方 `@vueuse/core` Rolldown pure annotation 警告。
  - `cd frontend && npm audit --audit-level=high`：0 vulnerabilities。
  - `cd frontend && npm run lint`：失败，项目尚未定义 lint 脚本。

---

## 2026-06-25 14:00 Asia/Shanghai P6-002 根据饮食计划和库存生成购物清单草稿

- 任务：P6-002 根据饮食计划和库存生成购物清单草稿
- 改动：
  - 后端：`AiProvider` 新增 `draftShoppingListFromMealPlan(List<MealPlan>, List<Recipe>, List<InventoryItem>)` 方法；`MockAiProvider` 新增菜谱食材解析与库存缺口计算，支持从 `ingredientsJson` 中读取食材名称、数量和单位，并在同单位库存不足时只生成差额采购项；`OpenAiProvider` 继续委托本地 mock 算法，不新增真实 AI 调用；`AiService` 新增按日期范围查询饮食计划、对应菜谱和库存的草稿方法；`AiController` 新增 `GET /api/ai/spaces/{spaceId}/meal-plan-shopping-draft?startDate=&endDate=` 端点。
  - 前端：`ai.ts` 新增 `draftShoppingListFromMealPlan()` API 方法；`MealPlanView.vue` 新增“生成采购清单”按钮、采购草稿面板和确认创建流程，用户确认后复用现有 `createShoppingList` / `addShoppingItem` API 写入业务数据。
  - 测试：新增 `MockAiProviderTest` 覆盖库存缺口计算和无计划提示；`AiServiceTests` 新增饮食计划购物草稿查询、空计划和日期校验测试；`ai.test.ts` 新增前端 API 路径与参数测试。
**验证**：
  - `cd backend && ./mvnw test -B -Dtest="AiServiceTests,MockAiProviderTest,OpenAiProviderTest"`：26 tests passed。
  - `cd frontend && npm test -- ai.test.ts`：1 个测试文件 6 tests passed。
  - `cd backend && ./mvnw test -B`：240 tests passed。
  - `cd frontend && npm test`：11 个测试文件 92 tests passed。
  - `cd frontend && npm run build`：通过；仍有既有 Rolldown pure annotation 和大 chunk 警告。
  - `python3 scripts/agent_changelog_archive.py`：通过。
  - `python3 scripts/agent_doc_check.py`：按预期返回“BACKLOG has no todo tasks”，触发当前阶段完成的停止条件。
  - `git diff --check`：通过。
- **文档更新**：`docs/BACKLOG.md`、`docs/CURRENT_STATE.md`、`docs/API_DESIGN.md`、`docs/ARCHITECTURE.md`、`docs/ROADMAP.md`、`docs/CHANGELOG_AGENT.md`。

---
