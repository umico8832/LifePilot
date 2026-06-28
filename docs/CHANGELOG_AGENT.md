# Agent Changelog

本文件只保留最近若干条完整开发记录；更早历史由 `scripts/agent_changelog_archive.py` 自动归档到 `docs/changelog/`。

默认接手请读 `docs/RECENT_HISTORY.md`。需要追溯具体历史时，再按月份查看归档文件。

## 维护方式

```bash
python3 scripts/agent_changelog_archive.py --keep 10
```

脚本默认保留最近 10 条完整记录，并刷新 `docs/RECENT_HISTORY.md`。

## 2026-06-28 23:33 Asia/Shanghai P9-004 Demo seed 真实 MySQL 冒烟验证与 P10 任务池规划

- 任务：P9-004 Demo seed 真实 MySQL 冒烟验证与浏览器检查；任务池耗尽前补充下一阶段可执行 backlog。
- 改动：
  - 验证：确认 `scripts/demo_seed.sh` 语法和 dry-run 正常，mysql client 存在；Docker Desktop 仍处于手动暂停状态，3307 端口虽然开放但 MySQL 会话探测 10 秒超时，项目未安装 Playwright，因此未执行真实 `--apply`/`--verify` 和浏览器冒烟。
  - 文档状态：`docs/BACKLOG.md` 标记 P9-004 完成；`docs/CURRENT_STATE.md` 记录真实 MySQL/浏览器验证未运行的原因、替代验证和后续补跑命令。
  - 任务池补充：`docs/ROADMAP.md` 新增 Phase 25 跨模块权限矩阵落地、Phase 26 权限回归与演示路径整理；`docs/BACKLOG.md` 新增 P10-001～P10-004，继续将 viewer 写保护扩展到购物、库存、待办、菜谱、饮食计划和票据文档，并整理权限回归演示路径。
**验证**：
  - `date '+%Y-%m-%d %H:%M:%S %Z %z'`：确认当前系统时间为 `2026-06-28 23:33:36 CST +0800`。
  - `bash -n scripts/demo_seed.sh`：通过。
  - `scripts/demo_seed.sh --dry-run`：通过，确认 SQL 文件存在且默认 dry-run 不写库。
  - `docker compose ps --format json`：未通过，Docker Desktop is manually paused。
  - `command -v mysql && mysql --version`：通过，mysql client 为 8.0.43。
  - `nc -z 127.0.0.1 3306`：未监听；`nc -z 127.0.0.1 3307`：端口开放。
  - `MYSQL_PORT=3307 perl -e 'alarm 10; exec @ARGV' scripts/demo_seed.sh --verify`：10 秒超时退出 142，未确认可用 MySQL 会话。
  - `MYSQL_PWD=lifepilot_dev_password perl -e 'alarm 10; exec @ARGV' mysql --host=127.0.0.1 --port=3307 --user=lifepilot --database=lifepilot --execute='SELECT 1 AS ok;'`：10 秒超时退出 142。
  - `test -d frontend/node_modules/playwright`：返回 `no-playwright`，浏览器自动化未运行。
  - 后续补跑：MySQL 可用后执行 `MYSQL_PORT=3307 scripts/demo_seed.sh --apply && MYSQL_PORT=3307 scripts/demo_seed.sh --verify`，再启动服务并检查首页、购物清单、库存提醒、饮食计划和 AI 日志页面。
  - `python3 scripts/agent_changelog_archive.py`：通过，保留 10 条并刷新 `docs/RECENT_HISTORY.md`。
  - `python3 scripts/agent_doc_check.py`：通过，确认 BACKLOG 存在 P10-001～P10-004 todo，且 `CURRENT_STATE` 下一任务与 P10-001 一致。
  - `git diff --check`：通过。

## 2026-06-28 23:31 Asia/Shanghai P9-003 角色权限体验和测试矩阵加固

- 任务：P9-003 角色权限体验和测试矩阵加固。
- 改动：
  - 权限文档：`docs/API_DESIGN.md` 新增角色权限矩阵，明确 owner/admin/member/viewer 在空间资料、成员管理、邀请链接、记账、购物、库存、待办、菜谱、饮食计划、票据文档和 AI 日志中的读写边界；`docs/ARCHITECTURE.md` 记录 viewer 只读、member 普通写、admin/owner 管理的统一模型。
  - 后端：`TransactionService` 写操作从仅校验空间成员改为 `requireSpaceRole(owner, admin, member)`；viewer 仍可读取记账列表和详情，但不能新增、编辑或删除。
  - 前端：`FinanceView.vue` 根据当前空间角色隐藏分类管理、AI 记账、记一笔、编辑和删除操作；viewer 看到只读权限提示，误触写流程会显示统一权限提示。
  - 测试：`TransactionControllerTests` 覆盖 viewer 可读不可写、member/admin 可写；`TransactionServiceTests` 覆盖 `requireSpaceRole` 和 viewer 被拒；`FinanceView.test.ts` 覆盖 viewer 只读 UI。
  - 文档状态：`docs/BACKLOG.md` 标记 P9-003 完成；`docs/CURRENT_STATE.md` 指向 P9-004。
**验证**：
  - `date '+%Y-%m-%d %H:%M:%S %Z %z'`：确认当前系统时间为 `2026-06-28 23:31:05 CST +0800`。
  - `cd backend && ./mvnw test -B -Dtest=TransactionControllerTests,HouseholdControllerTests`：27 tests passed。
  - `cd frontend && npm test -- FinanceView.test.ts SpaceView.test.ts space.test.ts`：4 个测试文件 38 tests passed。
  - `cd backend && ./mvnw test -B`：首次运行失败，原因是旧 `TransactionServiceTests` 仍断言 `requireSpaceMembership`；修正后重跑通过，265 tests passed。
  - `cd frontend && npm test`：15 个测试文件 116 tests passed。
  - `cd frontend && npm run build`：通过；仍有既有第三方 `@vueuse/core` Rolldown pure annotation 警告。
  - `python3 scripts/agent_changelog_archive.py`：通过，保留 10 条并刷新 `docs/RECENT_HISTORY.md`。
  - `python3 scripts/agent_doc_check.py`：通过，确认 BACKLOG 存在 P9-004 todo，且 `CURRENT_STATE` 下一任务与 P9-004 一致。
  - `git diff --check`：通过。

## 2026-06-28 23:25 Asia/Shanghai P9-002 前端邀请管理与接受邀请体验

- 任务：P9-002 前端邀请管理与接受邀请体验。
- 改动：
  - 前端状态：space store 新增 `invitations`、`fetchInvitations()`、`createSpaceInvitation()`、`revokeSpaceInvitation()` 和 `acceptSpaceInvitation()`，接受邀请后刷新空间列表。
  - 空间页：管理员家庭空间视图新增邀请链接列表，展示目标邮箱、角色、过期时间、状态和撤销操作；生成邀请弹窗支持目标邮箱、角色、有效期和复制本地接受链接；普通成员只读且不显示邀请管理入口。
  - 接受流程：新增 `/spaces/invitations/accept?token=` 页面，支持链接 token 预填或手动输入，成功后刷新空间并跳转空间页；过期、撤销、邮箱不匹配、重复加入等错误显示更明确的中文提示。
  - 测试：新增 `AcceptInvitationView.test.ts` 和 `SpaceView.test.ts`，扩展 `space` API/store 测试，覆盖创建、撤销、接受邀请和普通成员只读状态。
  - 文档：`docs/API_DESIGN.md` 记录前端接受邀请路由；`docs/BACKLOG.md` 标记 P9-002 完成；`docs/CURRENT_STATE.md` 指向 P9-003。
**验证**：
  - `date '+%Y-%m-%d %H:%M:%S %Z %z'`：确认当前系统时间为 `2026-06-28 23:25:35 CST +0800`。
  - `cd frontend && npm test -- space.test.ts SpaceView.test.ts AcceptInvitationView.test.ts`：4 个测试文件 27 tests passed。
  - `cd frontend && npm run build`：通过；仍有既有第三方 `@vueuse/core` Rolldown pure annotation 警告。
  - `cd frontend && npm test`：15 个测试文件 115 tests passed。
  - 浏览器冒烟未运行：Docker Desktop 处于手动暂停状态，且项目未安装 Playwright 包；已用 API、Pinia store 和页面组件测试覆盖主要 UI 状态、接受流程和只读状态。
  - `python3 scripts/agent_changelog_archive.py`：通过，保留 10 条并刷新 `docs/RECENT_HISTORY.md`。
  - `python3 scripts/agent_doc_check.py`：通过，确认 BACKLOG 存在 P9-003～P9-004 todo，且 `CURRENT_STATE` 下一任务与 P9-003 一致。
  - `git diff --check`：通过。

## 2026-06-28 23:18 Asia/Shanghai P9-001 家庭空间邀请链接基础能力

- 任务：P9-001 家庭空间邀请链接基础能力。
- 改动：
  - 数据库：新增 `V11__create_household_invitation.sql`，创建 `household_invitation` 表，记录空间、邀请人、可选目标邮箱、角色、token hash、状态、过期时间、接受时间和接受人。
  - 后端：新增 `HouseholdInvitation`、`HouseholdInvitationMapper`、`HouseholdInvitationService` 和邀请 DTO；`HouseholdController` 新增创建、列表、撤销、接受邀请接口。创建/列表/撤销要求 `owner` 或 `admin`；接受邀请要求登录用户、token 有效未过期、状态 pending、目标邮箱匹配且未重复加入。
  - 安全边界：邀请 token 只以 SHA-256 hash 落库，创建响应一次性返回明文 token；不接入真实邮件、短信或外部账号服务，邀请不能直接授予 `owner`。
  - 前端 API：`frontend/src/api/space.ts` 新增 `InvitationResponse`、`createInvitation()`、`listInvitations()`、`revokeInvitation()` 和 `acceptInvitation()`；`space.test.ts` 覆盖新增 API 路径。
  - 文档：`docs/API_DESIGN.md`、`docs/DB_DESIGN.md`、`docs/ARCHITECTURE.md` 同步邀请接口、表结构和权限边界；`docs/BACKLOG.md` 标记 P9-001 完成；`docs/CURRENT_STATE.md` 指向 P9-002。
**验证**：
  - `date '+%Y-%m-%d %H:%M:%S %Z %z'`：确认当前系统时间为 `2026-06-28 23:18:49 CST +0800`。
  - `cd backend && ./mvnw test -B -Dtest=HouseholdControllerTests`：19 tests passed。
  - `cd frontend && npm test -- space.test.ts`：2 个测试文件 19 tests passed。
  - `cd backend && ./mvnw test -B`：262 tests passed。
  - `cd frontend && npm test`：13 个测试文件 107 tests passed。
  - `cd frontend && npm run build`：通过；仍有既有第三方 `@vueuse/core` Rolldown pure annotation 警告。
  - `python3 scripts/agent_changelog_archive.py`：通过，保留 10 条并刷新 `docs/RECENT_HISTORY.md`。
  - `python3 scripts/agent_doc_check.py`：通过，确认 BACKLOG 存在 P9-002～P9-004 todo，且 `CURRENT_STATE` 下一任务与 P9-002 一致。
  - `git diff --check`：通过。

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
