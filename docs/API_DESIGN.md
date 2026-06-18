# API Design

## 统一响应结构

```json
{
  "success": true,
  "code": "OK",
  "message": "ok",
  "data": {}
}
```

错误响应：

```json
{
  "success": false,
  "code": "VALIDATION_ERROR",
  "message": "Invalid request",
  "data": null
}
```

## 认证方式

- 登录成功返回 JWT。
- 前端使用 `Authorization: Bearer <token>`。
- 除公开接口和健康检查外，业务接口默认需要认证。

## 错误码

- `OK`
- `VALIDATION_ERROR`
- `UNAUTHORIZED`
- `FORBIDDEN`
- `NOT_FOUND`
- `CONFLICT`
- `BUSINESS_ERROR`
- `INTERNAL_ERROR`

## 用户接口

- `POST /api/auth/register`
- `POST /api/auth/login`
- `GET /api/users/me`
- `PATCH /api/users/me`

当前已实现：`POST /api/auth/register`、`POST /api/auth/login`、`GET /api/users/me`。

## 生活空间接口

- `GET /api/spaces`
- `POST /api/spaces`
- `GET /api/spaces/{spaceId}`
- `PATCH /api/spaces/{spaceId}`
- `GET /api/spaces/{spaceId}/members`
- `POST /api/spaces/{spaceId}/members`
- `PATCH /api/spaces/{spaceId}/members/{memberId}`

## 记账接口

- `GET /api/spaces/{spaceId}/transactions`
- `POST /api/spaces/{spaceId}/transactions`
- `GET /api/spaces/{spaceId}/transactions/{id}`
- `PATCH /api/spaces/{spaceId}/transactions/{id}`
- `DELETE /api/spaces/{spaceId}/transactions/{id}`
- `GET /api/spaces/{spaceId}/transaction-categories`
- `POST /api/spaces/{spaceId}/transaction-categories`
- `GET /api/spaces/{spaceId}/budgets`
- `POST /api/spaces/{spaceId}/budgets`

## 购物清单接口

- `GET /api/spaces/{spaceId}/shopping-lists`
- `POST /api/spaces/{spaceId}/shopping-lists`
- `GET /api/spaces/{spaceId}/shopping-lists/{id}`
- `PATCH /api/spaces/{spaceId}/shopping-lists/{id}`
- `DELETE /api/spaces/{spaceId}/shopping-lists/{id}`
- `POST /api/spaces/{spaceId}/shopping-lists/{id}/items`
- `PATCH /api/spaces/{spaceId}/shopping-lists/{id}/items/{itemId}`
- `DELETE /api/spaces/{spaceId}/shopping-lists/{id}/items/{itemId}`

## 库存接口

- `GET /api/spaces/{spaceId}/inventory-items`
- `POST /api/spaces/{spaceId}/inventory-items`
- `GET /api/spaces/{spaceId}/inventory-items/{id}`
- `PATCH /api/spaces/{spaceId}/inventory-items/{id}`
- `DELETE /api/spaces/{spaceId}/inventory-items/{id}`
- `GET /api/spaces/{spaceId}/inventory-items/alerts`

## 待办接口 ✅

- `GET /api/spaces/{spaceId}/todo-tasks` ✅（支持 `?status=` 筛选）
- `POST /api/spaces/{spaceId}/todo-tasks` ✅
- `GET /api/spaces/{spaceId}/todo-tasks/{id}` ✅
- `PATCH /api/spaces/{spaceId}/todo-tasks/{id}` ✅
- `DELETE /api/spaces/{spaceId}/todo-tasks/{id}` ✅

## 菜谱接口 ✅

- `GET /api/spaces/{spaceId}/recipes` ✅
- `POST /api/spaces/{spaceId}/recipes` ✅
- `GET /api/spaces/{spaceId}/recipes/{id}` ✅
- `PATCH /api/spaces/{spaceId}/recipes/{id}` ✅
- `DELETE /api/spaces/{spaceId}/recipes/{id}` ✅

## 文档接口 ✅

- `GET /api/spaces/{spaceId}/documents` ✅（支持 `?type=` 筛选）
- `POST /api/spaces/{spaceId}/documents` ✅
- `GET /api/spaces/{spaceId}/documents/{id}` ✅
- `PATCH /api/spaces/{spaceId}/documents/{id}` ✅
- `DELETE /api/spaces/{spaceId}/documents/{id}` ✅

## AI 接口

- `POST /api/ai/spaces/{spaceId}/parse-transaction` ✅
- `POST /api/ai/spaces/{spaceId}/parse-shopping` ✅
- `POST /api/ai/spaces/{spaceId}/parse-todo` ✅
- `GET /api/ai/spaces/{spaceId}/monthly-report?year=&month=` ✅

AI 接口返回草稿，用户确认后再调用业务写入接口。

### AI Provider 配置

通过环境变量控制 AI provider 行为：

| 环境变量 | 默认值 | 说明 |
|---|---|---|
| `AI_PROVIDER` | `mock` | Provider 类型：`mock`（本地确定性解析）或 `openai`（调用外部 API） |
| `OPENAI_API_KEY` | 空 | OpenAI API Key，`provider=openai` 时必填 |
| `OPENAI_BASE_URL` | `https://api.openai.com/v1` | OpenAI-compatible API 基础 URL |
| `OPENAI_MODEL` | `gpt-4o-mini` | 使用的模型名称 |
| `OPENAI_TEMPERATURE` | `0.2` | 生成温度 |
| `OPENAI_MAX_TOKENS` | `1024` | 最大输出 token 数 |
| `OPENAI_TIMEOUT` | `30` | 请求超时（秒） |
| `OPENAI_RETRY_MAX` | `2` | 最大重试次数 |

**Mock 回退机制**：当 `AI_PROVIDER=openai` 但 `OPENAI_API_KEY` 为空时，自动回退到 `MockAiProvider` 并打印警告日志。

**安全规则**：
- API Key 只通过环境变量注入，禁止写入代码或配置文件。
- `.gitignore` 已包含 `.env`，不得提交真实密钥。
- AI 请求/响应日志必须脱敏。

## 统计接口

- `GET /api/spaces/{spaceId}/statistics/overview` ✅
- `GET /api/spaces/{spaceId}/statistics/finance/monthly` ✅
- `GET /api/spaces/{spaceId}/statistics/finance/categories` ✅（支持 `?year=&month=` 参数，返回支出/收入分类汇总）
- `GET /api/spaces/{spaceId}/statistics/inventory`
- `GET /api/spaces/{spaceId}/statistics/todos`
