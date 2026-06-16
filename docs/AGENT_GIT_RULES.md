# Agent Git Rules

## 密钥和环境文件

- 不提交真实密钥。
- 不提交真实 `.env`。
- `.env.example` 只能包含示例值。

## 提交原则

- 小步提交。
- 每次提交聚焦一个主题。
- 提交前检查 `git status --short`。
- 不回滚用户未要求回滚的改动。

## 提交前检查

```bash
git status --short
cd backend && mvn test
cd frontend && npm run build
```

## Commit Message 规范

格式：

```text
<type>(<可选作用域>): <中文简短描述>

<可选：详细说明>
<可选：Closes #42>
```

大多数提交一行即可。

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

## 处理冲突

- 先读取冲突文件并理解双方改动。
- 只解决当前任务相关冲突。
- 无法安全判断时触发停止条件，等待用户。

## 当前环境不能提交时

在最终输出里给出符合本规范的建议 commit message，并说明未提交原因。
