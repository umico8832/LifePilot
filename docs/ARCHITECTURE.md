# Architecture

## 前后端分离架构

LifePilot 使用前后端分离架构：

- 前端 Vue 3 通过 REST API 访问后端。
- 后端 Spring Boot 提供认证、业务接口、统计和 AI provider 封装。
- MySQL 存储业务数据。
- AI provider 默认使用 mock，后续通过配置切换到 OpenAI-compatible provider。

## 后端模块划分

- `common`：统一响应、异常、基础 DTO。
- `config`：应用配置、OpenAPI、跨域、MyBatis、Flyway。
- `security`：Spring Security、JWT、权限上下文。
- `auth`：注册、登录、刷新会话。
- `user`：用户资料和个人设置。
- `space`：个人空间、家庭空间、成员和权限。
- `finance`：记账、分类、预算。
- `shopping`：购物清单和清单项。
- `inventory`：库存物品和提醒。
- `todo`：生活待办。
- `recipe`：菜谱和饮食计划。
- `document`：票据与文件记录。
- `ai`：AI provider、结构化解析、调用日志。
- `statistics`：统计聚合和生活报告。

### AI 调用日志

`ai` 模块通过 `AiCallLogService` 为 AI 解析、月报、菜谱推荐和饮食计划采购草稿记录审计日志。日志保存 provider、scenario、空间、用户、prompt hash、脱敏请求/响应摘要、状态、耗时和错误摘要；自然语言输入不保存原文。日志查询仍通过空间成员权限校验，避免跨空间读取。

## 前端目录结构

- `src/api`：Axios 实例和接口函数。
- `src/assets`：静态资源。
- `src/components`：通用组件。
- `src/router`：路由配置和守卫。
- `src/stores`：Pinia 状态。
- `src/types`：共享类型。
- `src/utils`：工具函数。
- `src/views`：页面。
- `src/layouts`：布局。

## AI Provider 设计

后端应定义 `AiProvider` 接口，默认实现 `MockAiProvider`。AI 输出尽量结构化 JSON，用户确认后再写入业务数据。真实 provider 通过环境变量启用，API Key 不能写入代码或提交仓库。

### Provider 接口与当前实现

`AiProvider` 接口位于 `backend/ai/AiProvider.java`，当前定义自然语言解析和生活建议草稿方法：

- `parseTransaction(String text)`：自然语言 → 记账草稿
- `parseShoppingList(String text)`：自然语言 → 购物清单草稿
- `parseTodo(String text)`：自然语言 → 待办草稿
- `recommendRecipes(List<InventoryItem>, List<Recipe>)`：库存 + 菜谱 → 菜谱推荐
- `draftShoppingListFromMealPlan(List<MealPlan>, List<Recipe>, List<InventoryItem>)`：饮食计划 + 菜谱 + 库存 → 购物清单草稿

月度报告由 `AiService.generateMonthlyReport` 直接聚合业务数据生成，不经过 `AiProvider` 接口，未来可将文本润色和建议生成委托给真实 provider。

### Provider 切换策略

通过 `lifepilot.ai.provider` 配置项选择实现：

| 值 | 对应实现 | 说明 |
|---|---|---|
| `mock`（默认） | `MockAiProvider` | 确定性本地解析，不依赖外部服务，适合开发和测试 |
| `openai` | `OpenAiProvider` | 调用 OpenAI-compatible API（Chat Completions），需要配置 API Key 和 Base URL |

切换机制使用 Spring `@ConditionalOnProperty` 或手动 `@Bean` 条件注入。未识别的 provider 值应用启动失败并给出明确日志。

### OpenAI-compatible Provider 配置

```yaml
lifepilot:
  ai:
    provider: ${AI_PROVIDER:mock}
    openai:
      api-key: ${OPENAI_API_KEY:}          # 必填，真实 provider 启用时不能为空
      base-url: ${OPENAI_BASE_URL:https://api.openai.com/v1}
      model: ${OPENAI_MODEL:gpt-4o-mini}
      temperature: ${OPENAI_TEMPERATURE:0.2}
      max-tokens: ${OPENAI_MAX_TOKENS:1024}
      timeout-seconds: ${OPENAI_TIMEOUT:30}
      retry-max-attempts: ${OPENAI_RETRY_MAX:2}
```

### 安全边界

1. **API Key 不入代码**：`OPENAI_API_KEY` 只通过环境变量注入，不得写入代码、配置文件默认值或提交历史。`.gitignore` 已包含 `.env`。
2. **Mock 回退**：当 `provider=openai` 但 API Key 为空时，自动回退到 `MockAiProvider` 并打印警告日志。不会因缺少密钥导致服务不可用。
3. **请求超时和重试**：外部 API 调用必须有超时（默认 30s）和有限重试（默认 2 次），避免阻塞请求线程。
4. **输出结构化**：真实 provider 的 System Prompt 要求返回与 `TransactionDraftResponse` / `ShoppingDraftResponse` / `TodoDraftResponse` 一致的 JSON 结构，后端反序列化后标记 `needsReview=true` 供用户确认。
5. **日志脱敏**：AI 请求/响应日志不得包含 API Key 或用户敏感信息。
6. **费用控制**：文档明确默认模型为成本较低的 `gpt-4o-mini`，生产环境可配置更强大模型但需评估成本。

### 扩展点

- 未来可增加更多 provider 实现（如 Azure OpenAI、Anthropic、Ollama 本地模型）。
- `parseTransaction` / `parseShoppingList` / `parseTodo` 可统一改为异步流式返回（当前设计为同步）。
- 菜谱推荐和饮食计划采购草稿当前由本地确定性算法计算；真实 provider 启用时仍委托 mock 算法，避免引入不可控建议和外部成本。
- 月度报告的文本润色和建议生成可委托给真实 provider，当前由 `AiService` 硬编码模板。
- 后续可增加 `AiCallLog` 记录每次外部调用的 token 用量、耗时和结果摘要。
- 当前已落地 `AiCallLog` 的基础审计表和查询接口，后续真实 provider 可在此基础上扩展 token、费用、模型名和外部请求 ID。

## 权限模型

- 用户通过 JWT 鉴权。
- 业务数据归属某个生活空间。
- 家庭空间通过 `household_member` 控制成员角色。
- 家庭空间邀请通过 `household_invitation` 记录一次性邀请 token 的 hash、目标邮箱、授予角色、状态和过期时间；系统不依赖真实邮件或短信服务，创建接口只在响应中一次性返回明文 token。
- 后端接口必须校验当前用户是否属于目标空间。
- 空间成员管理由 `space` 模块负责，`owner` 和 `admin` 可添加成员、调整角色或移除成员；系统保护每个空间至少保留一名 `owner` 或 `admin`。
- 空间邀请管理同样只允许 `owner` 和 `admin` 创建、列表和撤销；已登录用户接受邀请后由后端写入或重新激活 `household_member`。
- 角色边界遵循“viewer 只读、member 普通写、admin/owner 管理”的模型：空间成员和邀请管理要求 `owner/admin`；普通业务模块默认 `owner/admin/member` 可写、`viewer` 只读；AI 日志仅提供只读审计视图。当前记账模块已经在后端强制 viewer 不可写，前端同步隐藏写操作。

## 数据流

1. 前端收集表单或自然语言输入。
2. 后端进行认证和参数校验。
3. 业务服务读取或写入 MySQL。
4. AI 类请求先进入 provider，得到结构化结果。
5. 用户确认 AI 结果后再写入业务表。
6. 统计模块聚合业务数据返回图表。

## 后期扩展点

- OpenAI-compatible AI provider。
- Redis 缓存和限流。
- 文件存储服务。
- OCR provider。
- GitHub Actions CI。
- 移动端或 PWA。
