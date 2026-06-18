# Agent Review Checklist

每次任务完成后，自审以下问题：

- 是否偏离 LifePilot 的 AI 生活管理中枢定位？
- 是否引入假数据冒充已完成真实功能？
- 文档是否与代码实际状态一致？
- 前端是否接入真实后端接口，或明确仍处于允许的 mock 阶段？
- 后端接口是否有认证和空间权限校验？
- 是否运行了对应测试或构建验证？
- 未能运行测试时是否记录原因？
- 是否存在真实密钥、真实 `.env` 或外部服务凭据？
- 是否涉及医疗、法律、投资、支付、自动购买等禁止能力？
- 是否需要更新 `docs/CURRENT_STATE.md`、`docs/BACKLOG.md`、`docs/CHANGELOG_AGENT.md`？
- 是否运行 `python3 scripts/agent_changelog_archive.py` 刷新 `docs/RECENT_HISTORY.md` 并自动归档旧历史？
- 是否运行 `python3 scripts/agent_doc_check.py` 并通过？
- 若提交涉及多文件、多模块、前后端联动、数据库、CI、AI 或测试体系，commit message 是否包含中文 bullet body？
- 是否可以继续下一项任务？
