# Auto Development Protocol

LifePilot 的目标是让 AI Agent 可以长期、连续、自主地维护和开发项目。除非触发明确停止条件，Agent 不应在完成一个中型任务后停下来询问用户下一步。

## 自主开发目标

- 持续推进产品从文档、骨架、核心功能到可演示版本。
- 每次任务都保持代码、测试、文档和交接状态一致。
- 让新 Agent 能通过文档快速恢复上下文。
- 避免真实密钥、外部付费服务、高风险生活建议和不可逆操作。

## Agent 自主开发循环

1. 阅读 `AGENTS.md`、`docs/CURRENT_STATE.md`、`docs/BACKLOG.md`、`docs/HANDOFF.md`、`docs/RECENT_HISTORY.md`。
2. 从 `docs/BACKLOG.md` 选择最高优先级且未阻塞的任务。
3. 阅读相关设计文档。
4. 实施最小可验证变更。
5. 运行任务要求的测试或构建验证。
6. 按改动类型更新必要文档。
7. 追加 `docs/CHANGELOG_AGENT.md`，运行 `python3 scripts/agent_changelog_archive.py` 刷新近期历史并自动归档旧历史。
8. 运行文档一致性检查（当前为 `python3 scripts/agent_doc_check.py`）。
9. 自审 `docs/AGENT_REVIEW_CHECKLIST.md`。
10. 未触发停止条件时，继续选择下一项任务。

## 任务选择规则

- 优先级顺序：P0 高于 P1，高于 P2，高于 P3。
- 阶段顺序：不跳过当前阶段的阻塞任务。
- 优先选择能解锁后续工作的任务。
- 不做与产品定位冲突的功能。
- 不为了演示做长期假接口；mock 只能用于明确标记的 AI provider 或开发占位。
- 如果 `docs/BACKLOG.md` 中没有 `todo` 任务，必须触发停止条件，等待用户补充任务或确认新阶段。

## 继续开发规则

- 完成一个任务后，必须更新状态、任务池和日志。
- 如果未触发停止条件，自动继续下一项任务。
- 当前任务足够明确时，合理默认决策并继续。
- 遇到普通实现细节，不频繁询问用户。

## 半自主提交规则

- 默认不主动提交；只有用户明确要求提交，或用户明确进入“继续开发 / 自主开发 / 按 backlog 做下一个任务”模式时，Agent 才可以在完成一个完整 backlog 任务后自动提交一次。
- 自动提交前必须满足：任务验收完成、验证通过或说明跳过原因、相关文档同步、工作区不包含无关用户改动、提交信息符合 `docs/AGENT_GIT_RULES.md`。
- 普通解释、分析、审查、调研、小范围文档讨论或用户未要求提交的改动，不自动提交。
- Agent 永远不能自动 push、force push、rebase、reset、删除分支或删除 tag；这些操作必须等待用户明确确认。

## 停止条件

只有以下情况允许停止等待用户：

1. 用户明确要求停止。
2. 上下文接近上限，需要新开对话。
3. 需要用户做重大产品决策。
4. 需要真实 API Key、数据库密码、支付账号、短信服务、邮箱服务等外部资源。
5. 涉及破坏性数据库迁移或不可逆数据操作。
6. 需要删除大量代码或进行大规模重构。
7. 连续多次测试失败且无法定位原因。
8. Git 出现冲突且无法安全自动解决。
9. 当前任务需求与已有文档发生重大冲突。
10. 所有当前阶段 P0/P1/P2 任务均已完成。
11. 文档与代码严重冲突，且无法判断哪个来源可信。
12. 工作区存在会被误提交的无关用户改动。
13. 需要 push、force push、rebase、reset、删除分支、删除 tag 或其他高风险 Git 操作。

除以上情况外，不要停下来问用户“下一步做什么”。

## 上下文接近上限处理

当判断上下文接近上限时，必须：

1. 更新 `docs/HANDOFF.md`。
2. 更新 `docs/CURRENT_STATE.md`。
3. 更新 `docs/CHANGELOG_AGENT.md`。
4. 创建或更新 `docs/NEXT_CHAT_PROMPT.md`。
5. 停止继续开发。
6. 提示用户新开对话，并把 `docs/NEXT_CHAT_PROMPT.md` 内容交给新 Agent。

## 文档维护规则

- `AGENTS.md` 只保留长期协作入口，不写临时进度。
- `docs/CURRENT_STATE.md` 是唯一当前状态源，只在这里记录当前阶段、阻塞项、最近验证和下一任务。
- `docs/BACKLOG.md` 是唯一任务源，只在这里记录任务状态、优先级、验收标准和后续任务建议。
- `docs/RECENT_HISTORY.md` 是默认接手阅读的短历史摘要，由 `scripts/agent_changelog_archive.py` 生成或刷新，不作为当前状态来源。
- `docs/CHANGELOG_AGENT.md` 是近期完整历史记录，每完成任务追加记录；旧记录由 `scripts/agent_changelog_archive.py` 自动归档到 `docs/changelog/`，不作为当前状态来源。
- `docs/HANDOFF.md` 是稳定运行手册，只保留项目定位、技术栈、运行/测试方式、端口注意事项和接手流程；不要复制当前阶段、下一任务或已完成清单。
- `docs/NEXT_CHAT_PROMPT.md` 是极简新对话入口，只引用权威文档；不要复制当前阶段、下一任务或长篇状态。
- 设计改变时更新对应设计文档和 `docs/DECISION_LOG.md`。

## 文档更新分层

- 每个完成的开发任务：更新 `docs/CURRENT_STATE.md`、`docs/BACKLOG.md`、`docs/CHANGELOG_AGENT.md`，并运行 `python3 scripts/agent_changelog_archive.py`。
- API 改动：同步 `docs/API_DESIGN.md`。
- 数据库或迁移改动：同步 `docs/DB_DESIGN.md`。
- 架构、模块边界或目录结构变化：同步 `docs/ARCHITECTURE.md`。
- 测试策略或验证命令变化：同步 `docs/TESTING.md`。
- Git、提交、自动开发流程或文档权威关系变化：同步对应规则文档和 `docs/DECISION_LOG.md`。
- 运行方式、端口、环境依赖或接手流程变化：同步 `docs/HANDOFF.md` 和 `docs/NEXT_CHAT_PROMPT.md`。

## 测试与验证规则

- 文档任务至少检查文件是否完整、互相引用是否正确。
- 文档历史任务运行 `python3 scripts/agent_changelog_archive.py` 和 `python3 scripts/agent_doc_check.py`。
- 后端任务优先运行 `cd backend && mvn test`。
- 前端任务优先运行 `cd frontend && npm install && npm run build`。
- 无法运行测试时，必须说明原因并记录到 `docs/CHANGELOG_AGENT.md`。
- 不编造测试通过结果。

## 禁止事项

- 不提交真实密钥或真实 `.env`。
- 不接入支付、短信、邮件、第三方登录、真实 OCR 付费服务。
- 不做医疗诊断、药品用法剂量建议、投资建议、法律判断。
- 不自动购买商品或自动下单。
- 不用伪代码冒充完成。
- 不为了测试通过弱化业务规则。

## 停止时输出格式

当触发停止条件，或当前回复必须结束时，输出：

1. 当前完成了什么。
2. 修改了哪些文件。
3. 运行了哪些验证。
4. 验证是否通过。
5. 当前遗留问题。
6. 是否触发停止条件。
7. 如果未触发停止条件，下一项自动任务是什么。
8. 如果上下文接近上限，说明 `docs/NEXT_CHAT_PROMPT.md` 已生成。
9. 建议 commit message。
