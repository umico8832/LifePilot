# Current State

## 当前阶段

Phase 12 完成 + P1-002 完成。Phase 0-6, 9-10, 12, P1-001, P1-002 全部完成。

## 当前最高优先级任务

P1-003 增加 OpenAPI 文档。

## 最近完成任务

- P0-001～P0-013：全部完成。
- P1-001 完善错误码和异常处理（GlobalExceptionHandler 新增 5 类异常处理器 + 5 项测试）。
- P1-002 增加 GitHub Actions CI（`.github/workflows/ci.yml`，后端 Maven test + 前端 npm build 两个并行 jobs）。

## 当前阻塞项

无。

## 下一项自动任务

P1-003 增加 OpenAPI 文档。

## 最近验证结果

- 后端 `./mvnw test`：通过，53 tests passed（Auth 3 + Health 1 + App 1 + Household 9 + Transaction 6 + Shopping 6 + Inventory 6 + AI 8 + Statistics 8 + ExceptionHandler 5）。
- 前端 `npm run build`：通过。
- Flyway 迁移 V1-V5 在 H2 测试数据库上通过。

## 注意事项

- GlobalExceptionHandler 已统一处理：BusinessException、MethodArgumentNotValidException、HttpMessageNotReadableException、MissingServletRequestParameterException、HttpRequestMethodNotSupportedException、HttpMediaTypeNotSupportedException、NoResourceFoundException、通用 Exception。
- 所有错误响应使用统一 `ApiResponse.error(code, message)` 结构。
- 不要提交真实 `.env` 或真实密钥。
- 本机 `8080` 和 `3306` 端口被占用，当前开发服务使用后端 `18081`、MySQL `3307` 和前端 `5173`。
- 前端构建存在大 chunk 提示，当前阶段不阻塞，后续页面增多时应做按路由分包。
- Maven wrapper 已生成（`backend/mvnw`），CI 使用 `./mvnw test -B`。
