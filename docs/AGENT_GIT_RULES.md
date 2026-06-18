# Agent Git Rules

## 触发条件

只要用户请求、任务计划或自动开发流程中出现以下任一动作，都必须先阅读本文件，再执行 Git 写操作：

- `git add`
- `git commit`
- `git commit --amend`
- `git push`
- 提交、更改提交信息、暂存、推送、发布

提交信息生成后，必须在执行提交前自检一次：格式符合 `<type>(<可选作用域>): <中文描述>`，且描述为中文。

## 密钥和环境文件

- 不提交真实密钥。
- 不提交真实 `.env`。
- `.env.example` 只能包含示例值。

## 提交原则

- 小步提交。
- 每次提交聚焦一个主题。
- 提交前检查 `git status --short`。
- 不回滚用户未要求回滚的改动。
- 默认不主动提交；只有用户明确要求提交，或明确进入自主开发模式时，才允许按 `docs/AUTO_DEV_PROTOCOL.md` 的半自主提交规则自动提交。
- 自动提交只允许在完成一个完整 backlog 任务、验证与文档同步完成、且工作区没有无关用户改动时执行。
- Agent 不自动 push、force push、rebase、reset、删除分支或删除 tag。

## 提交前检查

所有提交前必须执行：

```bash
git status --short
git diff --check
python3 scripts/agent_changelog_archive.py
python3 scripts/agent_doc_check.py
```

涉及代码、配置、依赖、构建、数据库迁移、前端资源或运行行为的改动，还必须执行：

```bash
cd backend && mvn test
cd frontend && npm run build
```

纯文档改动可以跳过后端测试和前端构建，但最终回复必须说明：

- 本次仅修改文档。
- 跳过了哪些代码验证命令。
- 已执行哪些文档、归档或格式检查。

## Commit Message 规范

格式：

```text
<type>(<可选作用域>): <中文简短描述>

<可选：中文 bullet 详细说明>
<可选：Closes #42>
```

### 详细度规则

- 小提交可以只写一行 subject，例如纯文档小修、单点 bug、配置微调。
- 中型提交建议写 body：涉及多个文件但仍聚焦一个主题时，用 3-5 条中文 bullet 概括主要改动。
- 大型提交必须写 body：涉及前后端联动、数据库迁移、AI 能力、CI、测试体系、文档框架或多个模块协作时，用中文 bullet 说明核心变化和验证结果。
- body 只写摘要级信息，不复制完整 changelog，不写无关实现流水账。
- body 优先覆盖：新增能力、关键文件/模块、兼容性或安全边界、测试/构建结果。


`type` 只能使用：

- `feat`：新功能
- `fix`：修 bug
- `docs`：文档
- `style`：代码格式，不影响逻辑
- `refactor`：重构
- `perf`：性能优化
- `test`：测试
- `chore`：构建、工具、依赖
- `ci`：CI/CD
- `security`：安全

要求：

- 提交信息必须使用中文描述。
- 修改涉及特定模块时优先添加作用域。
- 禁止使用 `fix bug`、`wip`、`修改了一些文件`。
- 禁止写成 `feat: 添加A顺便修了B` 这类混合事项描述。
- 每次提交必须聚焦一个主题；若改动天然分属多个主题，应拆成多个提交。

示例：

```text
feat(auth): 完成 JWT 登录注册流程
```

```text
docs(roadmap): 更新项目阶段进度
```

```text
fix(question): 修复题目选项保存失败问题
```

中大型提交示例：

```text
feat(ai): 实现购物清单、待办草稿和月报 AI mock 解析

- 新增购物清单草稿解析：ShoppingDraftResponse + parseShoppingList 端点
- 新增待办草稿解析：TodoDraftResponse + parseTodo 端点
- 新增月度生活报告生成：MonthlyReportResponse + monthly-report 端点
- MockAiProvider 支持中文分隔符、优先级关键词和截止日期推断
- 前端 ShoppingView/TodoView/HomeView 新增 AI 助手 UI
- 新增 AI 端点测试，后端测试通过
```

## 处理冲突

- 先读取冲突文件并理解双方改动。
- 只解决当前任务相关冲突。
- 无法安全判断时触发停止条件，等待用户。

## 当前环境不能提交时

在最终输出里给出符合本规范的建议 commit message，并说明未提交原因。
