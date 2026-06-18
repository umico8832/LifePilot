# Current State

## 当前阶段

P2-001 完成。Phase 0-17 全部完成，Phase 14（AI provider 配置骨架）完成。

## 当前最高优先级任务

无剩余 todo 任务。可新增 P2 任务继续开发。

## 最近完成任务

- P0-001～P0-013：全部完成。
- P1-001～P1-017：全部完成。
- P1-017 增加前端关键测试：接入 Vitest 4 + jsdom + @vue/test-utils；auth store 测试（9 项）、space store 测试（10 项）、http interceptor 测试（5 项），共 24 项测试全部通过。
- P2-001 规划真实 AI provider 配置骨架：ARCHITECTURE.md 新增 Provider 接口与当前实现、Provider 切换策略、OpenAI-compatible 配置项、安全边界和扩展点；API_DESIGN.md 新增 AI Provider 配置环境变量表和安全规则；DECISION_LOG.md 新增 provider 配置骨架决策记录。

## 当前阻塞项

无。

## 下一项自动任务

无剩余 todo 任务。

## 最近验证结果

- 后端 `./mvnw test`：通过，88 tests passed（含 22 项 AiControllerTests）。
- 前端 `npm test`：通过，3 个测试文件共 24 项测试全部通过。
- 前端 `npm run build`：通过（vue-tsc + vite build）。
- Flyway 迁移 V1-V8 在 H2 测试数据库上通过。
- 文档归档脚本 `python3 scripts/agent_changelog_archive.py`：通过。
- 文档一致性检查 `python3 scripts/agent_doc_check.py`：通过。

## 注意事项

- GlobalExceptionHandler 已统一处理所有常见异常类型。
- 所有错误响应使用统一 `ApiResponse.error(code, message)` 结构。
- BusinessException 默认映射为 400 Bad Request。
- 不要提交真实 `.env` 或真实密钥。
- 本机 `8080` 和 `3306` 端口被占用，当前开发服务使用后端 `18081`、MySQL `3307` 和前端 `5173`。
- Maven wrapper 已生成（`backend/mvnw`），CI 使用 `./mvnw test -B`。
- Swagger UI 可通过 `/swagger-ui.html` 访问（无需认证），OpenAPI JSON 可通过 `/v3/api-docs` 获取。
