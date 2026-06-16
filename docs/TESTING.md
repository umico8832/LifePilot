# Testing Strategy

## 后端单元测试策略

- Service 层测试业务规则。
- Utility 和 mapper helper 测试边界输入。
- Security 相关测试 token、权限和异常路径。

## 后端集成测试策略

- Controller 使用 Spring Boot Test 或 MockMvc。
- 需要数据库时优先使用 Testcontainers 或独立 test profile。
- Flyway 迁移必须在测试环境验证。

## 前端测试策略

- 构建验证：`npm run build`。
- 后续增加 Vitest 测试工具函数、Store 和关键组件。
- 重要表单需要验证空态、错误态和提交状态。

## API 联调验证

- 每个业务模块完成后至少验证前端调用真实后端接口。
- 不允许前端长期只依赖 mock 数据。
- AI mock provider 需明确标记，且输出结构必须接近真实 provider。

## 构建验证

```bash
cd backend && mvn test
cd frontend && npm install && npm run build
```

## 每类任务至少应该跑什么验证

- 文档任务：检查文件存在、Markdown 结构、交叉引用。
- 后端接口任务：`mvn test`，必要时手动 curl。
- 前端页面任务：`npm run build`，必要时浏览器检查。
- 数据库任务：迁移执行验证。
- AI 任务：结构化输出测试和降级路径测试。

## 测试无法运行时如何记录

在 `docs/CHANGELOG_AGENT.md` 写明：

- 未运行的命令。
- 无法运行的原因。
- 已完成的替代验证。
- 后续需要补跑的验证。

