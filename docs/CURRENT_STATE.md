# Current State

## 当前阶段

P1-016 完成。Phase 0-16 全部完成。

## 当前最高优先级任务

P1-017 增加前端关键测试。

## 最近完成任务

- P0-001～P0-013：全部完成。
- P1-001～P1-016：全部完成。
- P1-016 前端分类管理 UI 集成：FinanceView 新增分类管理对话框（支出/收入分类列表、创建新分类含图标、删除分类）+ 记账表单新增分类选择器（按类型自动筛选分类、切换类型时清空不匹配分类）+ 交易列表表格新增分类列 + 记账创建/编辑/确认 payload 携带 categoryId。

## 当前阻塞项

无。

## 下一项自动任务

P1-017 增加前端关键测试。

## 最近验证结果

- 后端 `./mvnw test`：通过，88 tests passed（含 22 项 AiControllerTests）。
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
