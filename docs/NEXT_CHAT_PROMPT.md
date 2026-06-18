# Next Chat Prompt

请接手 LifePilot 项目并继续自主开发。

先读：

1. `AGENTS.md`
2. `docs/AUTO_DEV_PROTOCOL.md`
3. `docs/CURRENT_STATE.md`
4. `docs/BACKLOG.md`
5. `docs/HANDOFF.md`
6. `docs/RECENT_HISTORY.md`

当前状态、下一任务和阻塞项以 `docs/CURRENT_STATE.md` 为准。
任务池、任务状态和验收标准以 `docs/BACKLOG.md` 为准。
最近历史摘要以 `docs/RECENT_HISTORY.md` 为准。完整历史按需查 `docs/CHANGELOG_AGENT.md` 和 `docs/changelog/`。

任务匹配时读取对应的 `agent-skills/*/SKILL.md` 和专项设计文档。

除非触发 `docs/AUTO_DEV_PROTOCOL.md` 中的停止条件，否则选择最高优先级、未阻塞的 `todo` 任务继续开发，并在完成后运行验证、`python3 scripts/agent_changelog_archive.py` 和 `python3 scripts/agent_doc_check.py`。
