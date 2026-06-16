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

## 权限模型

- 用户通过 JWT 鉴权。
- 业务数据归属某个生活空间。
- 家庭空间通过 `household_member` 控制成员角色。
- 后端接口必须校验当前用户是否属于目标空间。

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

