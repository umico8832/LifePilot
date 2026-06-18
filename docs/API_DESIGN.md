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

- `POST /api/ai/parse-transaction`
- `POST /api/ai/create-shopping-list-draft`
- `POST /api/ai/create-todo-draft`
- `POST /api/ai/monthly-report-draft`

AI 接口返回草稿，用户确认后再调用业务写入接口。

## 统计接口

- `GET /api/spaces/{spaceId}/statistics/overview` ✅
- `GET /api/spaces/{spaceId}/statistics/finance/monthly` ✅
- `GET /api/spaces/{spaceId}/statistics/finance/categories`
- `GET /api/spaces/{spaceId}/statistics/inventory`
- `GET /api/spaces/{spaceId}/statistics/todos`
