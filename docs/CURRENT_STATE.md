# Current State

## 当前阶段

Phase 6：家庭库存。Phase 0-5 文档/骨架/鉴权/空间/记账/分类/购物清单均已完成。

## 当前最高优先级任务

P0-011 实现库存物品 CRUD。

## 最近完成任务

- P0-001～P0-010：见前次记录。
- P0-010 实现购物清单 CRUD（后端 ShoppingService/ShoppingController，前端 shopping API 和 ShoppingView.vue，路由 /shopping）。

## 当前阻塞项

无。

## 下一项自动任务

P0-011 实现库存物品 CRUD。

## 最近验证结果

- 后端 `mvn test`：通过，26 tests passed（Auth 3 + Health 1 + App 1 + Household 9 + Transaction 6 + Shopping 6）。
- 前端 `npm run build`：通过。
- Flyway 迁移 V1-V4 在 H2 测试数据库上通过。

## 注意事项

- 购物清单 CRUD 后端已完成（POST/GET/PATCH/DELETE `/api/spaces/{spaceId}/shopping-lists` 和 `/{listId}/items`），前端购物清单管理页面已完成（/shopping 路由）。
- AI provider 当前只在文档中设计，业务实现阶段必须先做 mock。
- 不要提交真实 `.env` 或真实密钥。
- 本机 `8080` 和 `3306` 端口被占用，当前开发服务使用后端 `18081`、MySQL `3307` 和前端 `5173`。
- 前端构建存在大 chunk 提示，当前阶段不阻塞，后续页面增多时应做按路由分包。