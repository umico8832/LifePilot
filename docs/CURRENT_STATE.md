# Current State

## 当前阶段

Phase 10：首页统计面板。Phase 0-6, 9 文档/骨架/鉴权/空间/记账/分类/购物清单/库存/AI mock provider 均已完成。

## 当前最高优先级任务

P0-013 实现首页统计面板。

## 最近完成任务

- P0-001～P0-012：见前次记录。
- P0-012 实现自然语言记账 mock provider（后端 AiProvider 接口 + MockAiProvider + AiService + AiController，前端 ai API 和 FinanceView AI 记账流程，8 项 AI 测试）。

## 当前阻塞项

无。

## 下一项自动任务

P0-013 实现首页统计面板。

## 最近验证结果

- 后端 `mvn test`：通过，40 tests passed（Auth 3 + Health 1 + App 1 + Household 9 + Transaction 6 + Shopping 6 + Inventory 6 + AI 8）。
- 前端 `npm run build`：通过。
- Flyway 迁移 V1-V5 在 H2 测试数据库上通过。

## 注意事项

- 库存 CRUD 后端已完成（POST/GET/PATCH/DELETE `/api/spaces/{spaceId}/inventory-items` 和 `/alerts`），前端库存管理页面已完成（/inventory 路由）。
- AI mock provider 已实现自然语言记账解析（`POST /api/ai/spaces/{spaceId}/parse-transaction`），其余 AI 端点（shopping-list-draft、todo-draft、monthly-report-draft）尚未实现。
- 不要提交真实 `.env` 或真实密钥。
- 本机 `8080` 和 `3306` 端口被占用，当前开发服务使用后端 `18081`、MySQL `3307` 和前端 `5173`。
- 前端构建存在大 chunk 提示，当前阶段不阻塞，后续页面增多时应做按路由分包。
