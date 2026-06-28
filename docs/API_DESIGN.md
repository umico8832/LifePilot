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
- `PUT /api/users/me` ✅

当前已实现：`POST /api/auth/register`、`POST /api/auth/login`、`GET /api/users/me`、`PUT /api/users/me`。

## 生活空间接口

- `GET /api/spaces`
- `POST /api/spaces`
- `GET /api/spaces/{spaceId}`
- `PATCH /api/spaces/{spaceId}`
- `GET /api/spaces/{spaceId}/members`
- `POST /api/spaces/{spaceId}/members`
- `PATCH /api/spaces/{spaceId}/members/{memberId}`
- `DELETE /api/spaces/{spaceId}/members/{memberId}` ✅
- `POST /api/spaces/{spaceId}/invitations` ✅
- `GET /api/spaces/{spaceId}/invitations` ✅
- `DELETE /api/spaces/{spaceId}/invitations/{invitationId}` ✅
- `POST /api/spaces/invitations/accept` ✅

当前已实现成员管理接口：成员列表、按邮箱添加成员、更新成员角色、移除成员。读取成员列表要求当前用户是空间成员；添加、角色更新和移除要求当前用户是 `owner` 或 `admin`。角色取值为 `owner`、`admin`、`member`、`viewer`；更新或移除成员时必须至少保留一名 `owner` 或 `admin`。

当前已实现邀请链接接口：`owner` 或 `admin` 可创建、列出和撤销空间邀请；邀请记录保存 token hash，不保存明文 token。创建邀请时响应会一次性返回 `token` 供前端复制本地邀请链接；列表接口返回邀请元数据但 `token` 为 `null`。已登录用户通过 `POST /api/spaces/invitations/accept` 提交 token 接受邀请；过期、撤销、已接受、目标邮箱不匹配、重复加入和非法角色均返回统一错误响应。

创建邀请请求：

```json
{
  "targetEmail": "guest@example.com",
  "role": "member",
  "expiresInDays": 7
}
```

接受邀请请求：

```json
{
  "token": "one-time-token"
}
```

邀请响应字段：

- `id`、`householdId`、`invitedBy`
- `targetEmail`：可为空；非空时只允许该邮箱用户接受。
- `role`：`admin`、`member` 或 `viewer`；邀请不能直接授予 `owner`。
- `status`：`pending`、`revoked` 或 `accepted`。
- `expiresAt`、`acceptedAt`、`acceptedBy`、`createdAt`
- `token`：仅创建响应返回明文 token，列表和接受响应为 `null`。

前端接受邀请路由为 `/spaces/invitations/accept?token=`。该路由需要登录；未登录用户会先进入登录页，登录后回到原邀请链接继续确认。

## 角色权限矩阵

空间角色统一为 `owner`、`admin`、`member`、`viewer`。读权限均要求当前用户是目标空间 active 成员；写权限不能只依赖前端隐藏按钮，后端必须校验角色。

| 模块 | owner | admin | member | viewer |
|---|---|---|---|---|
| 空间资料 | 读写 | 读写 | 只读 | 只读 |
| 成员管理 | 读写，可调整角色/移除成员 | 读写，可调整角色/移除成员 | 只读成员列表 | 只读成员列表 |
| 邀请链接 | 创建、列表、撤销 | 创建、列表、撤销 | 不可管理 | 不可管理 |
| 记账 | 读写 | 读写 | 读写 | 只读 |
| 购物清单 | 读写 | 读写 | 读写 | 只读 |
| 库存 | 读写 | 读写 | 读写 | 只读 |
| 待办 | 读写 | 读写 | 读写 | 只读 |
| 菜谱 | 读写 | 读写 | 读写 | 只读 |
| 饮食计划 | 读写 | 读写 | 读写 | 只读 |
| 票据文档 | 读写 | 读写 | 读写 | 只读 |
| AI 日志 | 只读 | 只读 | 只读 | 只读 |

当前后端已强制校验：空间成员/邀请管理只允许 `owner/admin` 管理；记账写操作只允许 `owner/admin/member`，`viewer` 可读取但不可新增、编辑或删除。其他业务模块的 viewer 写保护按该矩阵作为后续加固标准。

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
- `GET /api/ai/spaces/{spaceId}/recommend-recipes` ✅
- `GET /api/ai/spaces/{spaceId}/meal-plan-shopping-draft?startDate=&endDate=` ✅
- `GET /api/ai/spaces/{spaceId}/call-logs?scenario=&status=&limit=` ✅
- `GET /api/ai/spaces/{spaceId}/call-logs/summary?days=` ✅

AI 接口返回草稿，用户确认后再调用业务写入接口。

### AI 调用日志

`GET /api/ai/spaces/{spaceId}/call-logs?scenario=&status=&limit=`

返回当前用户所属空间内的 AI 调用审计记录，默认按创建时间倒序返回最近 50 条，`limit` 上限为 100。可选 `scenario` 和 `status` 过滤。

前端已通过 `/ai-logs` 页面接入该接口，展示空间内调用记录、脱敏请求/响应摘要、状态、耗时和错误摘要；页面不展示自然语言输入原文。

返回字段：

- `id`、`userId`、`spaceId`
- `provider`：`mock`、`openai` 或自定义 provider 类名。
- `scenario`：如 `parse_transaction`、`parse_shopping`、`parse_todo`、`monthly_report`、`recommend_recipes`、`meal_plan_shopping_draft`。
- `promptHash`：自然语言输入的 SHA-256 hash；非自然语言场景可为空。
- `requestJson` / `responseJson`：脱敏后的摘要 JSON，不保存自然语言输入原文。
- `status`：`success` 或 `failed`。
- `durationMs`、`errorMessage`、`createdAt`。

### AI 调用日志统计摘要

`GET /api/ai/spaces/{spaceId}/call-logs/summary?days=`

返回当前用户所属空间内最近一段时间的 AI 调用审计摘要。`days` 默认 30 天，后端限制在 1～365 天之间。

返回字段：

- `totalCount`：统计范围内总调用数。
- `successCount` / `failedCount`：成功和失败调用数。
- `successRate`：成功率，0～1 小数。
- `averageDurationMs`：平均耗时，单位毫秒；无日志时为 0。
- `scenarioCounts`：按 `scenario` 分组的调用量列表。
- `statusCounts`：按 `status` 分组的调用量列表。

### 饮食计划生成采购草稿

`GET /api/ai/spaces/{spaceId}/meal-plan-shopping-draft?startDate=YYYY-MM-DD&endDate=YYYY-MM-DD`

返回 `ShoppingDraftResponse`，字段与自然语言购物解析草稿一致：

- `listName`：默认“饮食计划采购清单”。
- `items`：根据饮食计划中的菜谱食材和当前库存缺口生成，包含 `name`、`quantity`、`unit`、`estimatedPrice`。
- `needsReview`：固定要求用户确认，尤其是菜谱食材单位可能不标准。
- `validationMessage`：说明无饮食计划、菜谱不完整、库存已覆盖或需确认后创建等状态。

该接口只生成草稿，不直接写入 `shopping_list` 或 `shopping_item`；前端确认后调用购物清单业务接口创建。

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
- `GET /api/spaces/{spaceId}/statistics/shopping` ✅（返回购物清单统计：总数、进行中/已完成、物品采购比、30 天趋势）
- `GET /api/spaces/{spaceId}/statistics/inventory` ✅（返回库存总数、低库存数量和分类分布）
- `GET /api/spaces/{spaceId}/statistics/inventory/alerts` ✅（返回临期物品列表和低库存物品列表）
- `GET /api/spaces/{spaceId}/statistics/todos` ✅（返回待办状态计数、逾期数量、完成率和近 30 天完成趋势）
