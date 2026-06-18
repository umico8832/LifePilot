# LifePilot Agent Guide

LifePilot 采用 AI Agent 自主持续开发模式。新 Agent 接手时，应先读本文件，再按任务类型阅读 docs 目录中的专项文档。

## 必读顺序

1. `docs/AUTO_DEV_PROTOCOL.md`：自主开发循环、停止条件、文档维护规则。
2. `docs/CURRENT_STATE.md`：当前阶段、最高优先级任务、阻塞项、下一项自动任务。
3. `docs/BACKLOG.md`：可执行任务池和任务选择规则。
4. `docs/HANDOFF.md`：交接信息、运行方式、测试方式、续接提示词。
5. `docs/RECENT_HISTORY.md`：最近开发摘要。

权威关系：

- 当前状态、下一任务、阻塞项只以 `docs/CURRENT_STATE.md` 为准。
- 任务池、任务状态和验收标准只以 `docs/BACKLOG.md` 为准。
- 最近历史摘要以 `docs/RECENT_HISTORY.md` 为准。
- 完整历史记录以 `docs/CHANGELOG_AGENT.md` 和 `docs/changelog/` 归档为准，按需读取，不作为默认必读全文。
- `docs/HANDOFF.md` 和 `docs/NEXT_CHAT_PROMPT.md` 不复制当前状态，只提供接手入口和运行信息。

## Git 操作硬规则

任何涉及 `git add`、`git commit`、`git push`、提交、更改提交信息、暂存、推送或发布的任务，必须先阅读 `docs/AGENT_GIT_RULES.md`，再执行 Git 写操作。

执行提交前，必须确认提交信息符合 `docs/AGENT_GIT_RULES.md` 的中文 Commit Message 规范；不允许使用英文描述作为提交信息。

## 按任务类型继续阅读

- 产品和范围：`docs/PRODUCT_STRATEGY.md`、`docs/PRD.md`、`docs/ROADMAP.md`
- 架构和模块：`docs/ARCHITECTURE.md`
- 数据库：`docs/DB_DESIGN.md`
- API：`docs/API_DESIGN.md`
- 测试：`docs/TESTING.md`
- Git：`docs/AGENT_GIT_RULES.md`
- 自审：`docs/AGENT_REVIEW_CHECKLIST.md`
- 技术决策：`docs/DECISION_LOG.md`

## 项目 Skills

项目内可复用 Agent 技能放在 `agent-skills/*/SKILL.md`。当任务匹配时，先阅读对应 skill，再继续阅读专项文档：

- `agent-skills/lifepilot-auto-dev/SKILL.md`：继续自主开发、接手 backlog、执行交接。
- `agent-skills/lifepilot-backend-module/SKILL.md`：实现 Spring Boot 后端业务模块、权限和测试。
- `agent-skills/lifepilot-api-data-contract/SKILL.md`：设计或审查 API、数据库迁移、前端 API 类型契约。
- `agent-skills/lifepilot-ai-mock-provider/SKILL.md`：实现 AI mock provider、结构化草稿和用户确认流程。
- `agent-skills/lifepilot-frontend-design/SKILL.md`：设计或改造 Vue 前端界面、页面状态、响应式体验。
- `agent-skills/lifepilot-webapp-testing/SKILL.md`：验证本地前端、浏览器交互、console、前后端联调。
- `agent-skills/lifepilot-doc-coauthoring/SKILL.md`：编写、重构或同步项目文档。

## 协作原则

- 除非触发 `docs/AUTO_DEV_PROTOCOL.md` 中的停止条件，否则完成一个任务后应继续从 `docs/BACKLOG.md` 选择下一项最高优先级任务。
- 不要每完成一个中型任务就停下来问用户“下一步做什么”。
- 当前阶段、临时 TODO、开发进度不要写入本文件。
- 当前状态写入 `docs/CURRENT_STATE.md`。
- 任务池写入 `docs/BACKLOG.md`。
- 开发日志写入 `docs/CHANGELOG_AGENT.md`，再运行 `python3 scripts/agent_changelog_archive.py` 刷新 `docs/RECENT_HISTORY.md` 并自动归档旧历史。
- 运行方式或接手流程变化时更新 `docs/HANDOFF.md`。
- 新对话入口变化时更新 `docs/NEXT_CHAT_PROMPT.md`，但不要在其中复制当前任务状态。
