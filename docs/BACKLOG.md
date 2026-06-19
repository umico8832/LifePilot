# Backlog

状态值：`todo`、`in_progress`、`done`、`blocked`。

本文件采用机器化任务格式，供 Agent 自动选择任务、判断验证命令和更新文档。新增或重写任务时应使用固定字段：

- 优先级
- 状态
- 所属阶段
- 目标
- 涉及文件/模块
- 验收标准
- 验证命令
- 完成后更新
- 是否阻塞后续任务
- 完成后建议下一任务

Agent 选择任务时，优先执行最高优先级、最低阶段编号、未阻塞的 `todo` 任务。任务完成后，更新本文件、`docs/CURRENT_STATE.md` 和 `docs/CHANGELOG_AGENT.md`，再运行 `python3 scripts/agent_changelog_archive.py` 刷新近期历史。

## P0 Tasks

### P0-001 创建项目文档体系

- 优先级：P0
- 状态：done
- 所属阶段：Phase 0
- 目标：建立 Agent 可长期维护的文档入口、协议、规划、状态和交接文件。
- 涉及文件/模块：`AGENTS.md`、`README.md`、`docs/*`
- 验收标准：必需文档全部存在，且不声称未实现功能已完成。
- 是否阻塞后续任务：是
- 完成后建议下一任务：P0-002 创建项目骨架

### P0-002 创建项目骨架

- 优先级：P0
- 状态：done
- 所属阶段：Phase 1
- 目标：创建后端、前端、Docker Compose、环境变量示例和启动说明。
- 涉及文件/模块：`backend/`、`frontend/`、`docker-compose.yml`、`.env.example`、`README.md`
- 验收标准：目录结构完整，后端健康检查存在，前端首页存在。
- 是否阻塞后续任务：是
- 完成后建议下一任务：P0-003 配置后端 Spring Boot

### P0-003 配置后端 Spring Boot

- 优先级：P0
- 状态：done
- 所属阶段：Phase 1
- 目标：配置 Spring Boot 3、Validation、Security、MyBatis-Plus、Flyway 和测试基础。
- 涉及文件/模块：`backend/pom.xml`、`backend/src/main/**`、`backend/src/test/**`
- 验收标准：`cd backend && mvn test` 通过。
- 是否阻塞后续任务：是
- 完成后建议下一任务：P0-004 配置前端 Vue 3

### P0-004 配置前端 Vue 3

- 优先级：P0
- 状态：done
- 所属阶段：Phase 1
- 目标：配置 Vue 3、TypeScript、Vite、Router、Pinia、Element Plus、Axios、ECharts。
- 涉及文件/模块：`frontend/package.json`、`frontend/src/**`
- 验收标准：`cd frontend && npm install && npm run build` 通过。
- 是否阻塞后续任务：是
- 完成后建议下一任务：P0-005 实现用户注册登录

### P0-005 实现用户注册登录

- 优先级：P0
- 状态：done
- 所属阶段：Phase 2
- 目标：实现注册、登录、JWT 鉴权和当前用户接口。
- 涉及文件/模块：`backend/auth`、`backend/user`、`backend/security`、`frontend/src/views`
- 验收标准：用户可注册登录，受保护接口需要 JWT。
- 是否阻塞后续任务：是
- 完成后建议下一任务：P0-006 实现生活空间模型

### P0-006 实现生活空间模型

- 优先级：P0
- 状态：done
- 所属阶段：Phase 3
- 目标：实现个人空间、家庭空间和成员权限基础模型。
- 涉及文件/模块：`backend/space`、`frontend/src/stores`
- 验收标准：用户只能访问所属空间数据。
- 是否阻塞后续任务：是
- 完成后建议下一任务：P0-007 实现支出记录 CRUD

### P0-007 实现支出记录 CRUD

- 优先级：P0
- 状态：done
- 所属阶段：Phase 4
- 目标：实现空间内支出记录增删改查。
- 涉及文件/模块：`backend/finance`、`frontend/src/views`
- 验收标准：前后端可创建、查看、更新、删除支出记录。
- 是否阻塞后续任务：是
- 完成后建议下一任务：P0-008 实现收入记录 CRUD

### P0-008 实现收入记录 CRUD

- 优先级：P0
- 状态：done
- 所属阶段：Phase 4
- 目标：实现空间内收入记录增删改查。
- 涉及文件/模块：`backend/finance`、`frontend/src/views`
- 验收标准：前后端可管理收入记录。
- 是否阻塞后续任务：否
- 完成后建议下一任务：P0-009 实现消费分类管理

### P0-009 实现消费分类管理

- 优先级：P0
- 状态：done
- 所属阶段：Phase 4
- 目标：实现收入和支出分类管理。
- 涉及文件/模块：`backend/finance`、`frontend/src/views`
- 验收标准：用户可维护分类并在记录中使用。
- 是否阻塞后续任务：否
- 完成后建议下一任务：P0-010 实现购物清单 CRUD

### P0-010 实现购物清单 CRUD

- 优先级：P0
- 状态：done
- 所属阶段：Phase 5
- 目标：实现购物清单和清单项管理。
- 涉及文件/模块：`backend/shopping`、`frontend/src/views`
- 验收标准：用户可维护购物清单和采购状态。
- 是否阻塞后续任务：否
- 完成后建议下一任务：P0-011 实现库存物品 CRUD

### P0-011 实现库存物品 CRUD

- 优先级：P0
- 状态：done
- 所属阶段：Phase 6
- 目标：实现库存物品、分类、位置和基础提醒字段。
- 涉及文件/模块：`backend/inventory`、`frontend/src/views`
- 验收标准：用户可维护库存物品。
- 是否阻塞后续任务：否
- 完成后建议下一任务：P0-012 实现自然语言记账 mock provider

### P0-012 实现自然语言记账 mock provider

- 优先级：P0
- 状态：done
- 所属阶段：Phase 9
- 目标：实现 AiProvider 接口和自然语言记账 mock 解析。
- 涉及文件/模块：`backend/ai`、`frontend/src/api`
- 验收标准：AI 输出结构化 JSON，用户确认后可写入记账。
- 是否阻塞后续任务：否
- 完成后建议下一任务：P0-013 实现首页统计面板

### P0-013 实现首页统计面板

- 优先级：P0
- 状态：done
- 所属阶段：Phase 10
- 目标：实现基础统计接口和前端首页统计。
- 涉及文件/模块：`backend/statistics`、`frontend/src/views`
- 验收标准：统计数据来自后端真实接口。
- 是否阻塞后续任务：否
- 完成后建议下一任务：P1-001 完善错误码和异常处理

## P1 Tasks

### P1-001 完善错误码和异常处理

- 优先级：P1
- 状态：done
- 所属阶段：Phase 2
- 目标：统一业务异常、校验异常和错误响应。
- 涉及文件/模块：`backend/common`
- 验收标准：接口错误使用统一响应结构。
- 是否阻塞后续任务：否
- 完成后建议下一任务：P1-002 增加 GitHub Actions CI

### P1-002 增加 GitHub Actions CI

- 优先级：P1
- 状态：done
- 所属阶段：Phase 12
- 目标：添加后端测试和前端构建 CI。
- 涉及文件/模块：`.github/workflows/ci.yml`、`backend/.mvn/wrapper/`、`backend/mvnw`
- 验收标准：CI 可运行 Maven test 和 npm build。
- 是否阻塞后续任务：否
- 完成后建议下一任务：P1-003 增加 OpenAPI 文档

### P1-003 增加 OpenAPI 文档

- 优先级：P1
- 状态：done
- 所属阶段：Phase 2
- 目标：接入 springdoc-openapi。
- 涉及文件/模块：`backend/pom.xml`、`backend/config/OpenApiConfig.java`、`backend/config/SecurityConfig.java`
- 验收标准：本地可访问 `/swagger-ui.html` 和 `/v3/api-docs`，Security 配置放行 swagger 路径。
- 是否阻塞后续任务：否
- 完成后建议下一任务：P1-004 完善前端空态和错误态

### P1-004 完善前端空态和错误态

- 优先级：P1
- 状态：done
- 所属阶段：Phase 2
- 目标：所有 CRUD 页面增加 loading、empty、error 和 no-space 状态提示。
- 涉及文件/模块：`frontend/src/views`
- 验收标准：用户在空数据、加载失败或未选空间时看到有意义的提示和引导。
- 是否阻塞后续任务：否
- 完成后建议下一任务：P1-005 增加前端路由守卫和未登录重定向

### P1-005 增加前端路由守卫和未登录重定向

- 优先级：P1
- 状态：done
- 所属阶段：Phase 2
- 目标：未登录用户访问受保护路由时重定向到登录页；登录后跳回原页面。
- 涉及文件/模块：`frontend/src/router/index.ts`、`frontend/src/api/http.ts`、`frontend/src/views/auth/AuthView.vue`
- 验收标准：未登录访问 `/`、`/spaces`、`/finance` 等页面自动跳转 `/auth`；401 响应触发登出和跳转。
- 是否阻塞后续任务：否
- 完成后建议下一任务：P1-006 前端大 chunk 分包（按路由懒加载）

### P1-006 前端大 chunk 分包（按路由懒加载）

- 优先级：P1
- 状态：done
- 所属阶段：Phase 2
- 目标：按路由懒加载拆分前端 bundle，消除大 chunk 警告。
- 涉及文件/模块：`frontend/src/router/index.ts`
- 验收标准：`npm run build` 无 500 kB 以上 chunk 警告，每个路由独立成 chunk。
- 是否阻塞后续任务：否
- 完成后建议下一任务：P1-007 完善 AppShell 导航栏（登录状态显示和退出按钮）

### P1-007 完善 AppShell 导航栏

- 优先级：P1
- 状态：done
- 所属阶段：Phase 2
- 目标：侧边栏导航改为路由链接，显示当前路由高亮、用户名称和退出登录按钮。
- 涉及文件/模块：`frontend/src/layouts/AppShell.vue`、`frontend/src/styles.css`
- 验收标准：导航项点击可跳转路由，当前页高亮，已登录显示用户名和退出按钮。
- 是否阻塞后续任务：否
- 完成后建议下一任务：P1-008 前端视口宽度适配完善

### P1-008 前端视口宽度适配完善

- 优先级：P1
- 状态：done
- 所属阶段：Phase 2
- 目标：所有页面在桌面、平板和手机宽度下布局正确、无溢出。
- 涉及文件/模块：`frontend/src/views`、`frontend/src/layouts/AppShell.vue`、`frontend/src/styles.css`
- 验收标准：`npm run build` 通过；主要页面在 1440px、1024px、768px 和 375px 宽度下无水平溢出。
- 是否阻塞后续任务：否
- 完成后建议下一任务：P1-009 实现生活待办 CRUD

### P1-009 实现生活待办 CRUD

- 优先级：P1
- 状态：done
- 所属阶段：Phase 7
- 目标：实现生活待办任务的增删改查、状态流转、优先级和截止日期管理。
- 涉及文件/模块：`backend/todo`、`backend/src/main/resources/db/migration/V6__create_todo_task.sql`、`frontend/src/api/todo.ts`、`frontend/src/views/todo/TodoView.vue`、`frontend/src/router/index.ts`、`frontend/src/layouts/AppShell.vue`
- 验收标准：前后端可创建、查看、更新、删除待办任务；支持状态流转（待处理→进行中→已完成/已取消）；支持按状态筛选；导航栏显示待办入口。
- 是否阻塞后续任务：否
- 完成后建议下一任务：P1-010 实现菜谱管理 CRUD

### P1-010 实现菜谱管理 CRUD

- 优先级：P1
- 状态：done
- 所属阶段：Phase 8
- 目标：实现菜谱的增删改查，支持食材 JSON 和步骤 JSON 存储。
- 涉及文件/模块：`backend/recipe`、`backend/src/main/resources/db/migration/V7__create_recipe.sql`、`frontend/src/api/recipe.ts`、`frontend/src/views/recipe/RecipeView.vue`、`frontend/src/router/index.ts`、`frontend/src/layouts/AppShell.vue`
- 验收标准：前后端可创建、查看、更新、删除菜谱；菜谱包含名称、描述、食材 JSON、步骤 JSON；导航栏显示菜谱入口。
- 是否阻塞后续任务：否
- 完成后建议下一任务：P1-011 实现票据与文件管理 CRUD

### P1-011 实现票据与文件管理 CRUD

- 优先级：P1
- 状态：done
- 所属阶段：Phase 11
- 目标：实现票据、收据、保修卡、合同、说明书和证件索引的增删改查，支持按类型筛选和到期提醒。
- 涉及文件/模块：`backend/document`、`backend/src/main/resources/db/migration/V8__create_document_record.sql`、`frontend/src/api/document.ts`、`frontend/src/views/document/DocumentView.vue`、`frontend/src/router/index.ts`、`frontend/src/layouts/AppShell.vue`
- 验收标准：前后端可创建、查看、更新、删除文档记录；支持 invoice/receipt/warranty/contract/manual/certificate/other 类型；支持按类型筛选；即将过期（30天内）标识；导航栏显示文档入口。
- 是否阻塞后续任务：否
- 完成后建议下一任务：P1-012 扩展 AI mock provider：购物清单草稿

### P1-012 扩展 AI mock provider：购物清单草稿

- 优先级：P1
- 状态：done
- 所属阶段：Phase 9
- 目标：实现自然语言生成购物清单草稿，用户确认后写入购物清单。
- 涉及文件/模块：`backend/ai`、`backend/shopping`、`frontend/src/api/ai.ts`、`frontend/src/views/shopping/ShoppingView.vue`
- 验收标准：AI 输出结构化购物清单草稿；用户可编辑确认后创建真实购物清单和清单项；mock 行为确定且有测试覆盖。
- 是否阻塞后续任务：否
- 完成后建议下一任务：P1-013 扩展 AI mock provider：待办草稿

### P1-013 扩展 AI mock provider：待办草稿

- 优先级：P1
- 状态：done
- 所属阶段：Phase 9
- 目标：实现自然语言生成待办任务草稿，用户确认后写入待办模块。
- 涉及文件/模块：`backend/ai`、`backend/todo`、`frontend/src/api/ai.ts`、`frontend/src/views/todo/TodoView.vue`
- 验收标准：AI 输出结构化待办草稿；支持标题、描述、优先级、截止时间的确定性 mock 解析；用户确认后创建真实待办。
- 是否阻塞后续任务：否
- 完成后建议下一任务：P1-014 扩展 AI mock provider：月报草稿

### P1-014 扩展 AI mock provider：月报草稿

- 优先级：P1
- 状态：done
- 所属阶段：Phase 9
- 目标：基于真实统计数据生成月度生活报告草稿。
- 涉及文件/模块：`backend/ai`、`backend/statistics`、`frontend/src/api/ai.ts`、`frontend/src/views/HomeView.vue`
- 验收标准：月报草稿来自真实统计接口或服务聚合数据；输出结构包含财务概览、库存提醒、购物/待办摘要和建议；不接真实外部 AI。
- 是否阻塞后续任务：否
- 完成后建议下一任务：P1-015 增加更多统计接口

### P1-015 增加更多统计接口

- 优先级：P1
- 状态：done
- 所属阶段：Phase 10
- 目标：补齐分类财务、库存、待办等统计接口。
- 涉及文件/模块：`backend/statistics`、`frontend/src/api/statistics.ts`、`frontend/src/views/HomeView.vue`
- 验收标准：实现并测试 `/statistics/finance/categories`、`/statistics/inventory`、`/statistics/todos`；前端可展示真实统计数据。
- 是否阻塞后续任务：否
- 完成后建议下一任务：P1-016 前端分类管理 UI 集成

### P1-016 前端分类管理 UI 集成

- 优先级：P1
- 状态：done
- 所属阶段：Phase 4
- 目标：在 FinanceView 中集成收入/支出分类管理 UI。
- 涉及文件/模块：`frontend/src/views/finance/FinanceView.vue`、`frontend/src/api/transaction.ts`
- 验收标准：用户可在前端创建、查看、删除分类，并在记账表单中选择真实分类；包含加载、空态和错误态。
- 是否阻塞后续任务：否
- 完成后建议下一任务：P1-017 增加前端关键测试

### P1-017 增加前端关键测试

- 优先级：P1
- 状态：done
- 所属阶段：Phase 13
- 目标：为前端关键 store、API helper 或核心页面交互增加测试基础。
- 涉及文件/模块：`frontend/package.json`、`frontend/src/stores`、`frontend/src/api`、`frontend/src/views`
- 验收标准：接入合适的前端测试工具；至少覆盖认证 store、空间 store 或一个关键 API/helper；`npm run build` 和测试命令均通过。
- 验证命令：`cd frontend && npm run build`；新增测试命令按实际工具补充；`python3 scripts/agent_changelog_archive.py`；`python3 scripts/agent_doc_check.py`
- 完成后更新：`docs/BACKLOG.md`、`docs/CURRENT_STATE.md`、`docs/CHANGELOG_AGENT.md`、`docs/TESTING.md`（如新增测试工具或命令）
- 是否阻塞后续任务：否
- 完成后建议下一任务：P2-001 规划真实 AI provider 配置骨架

## P2 Tasks

### P2-001 规划真实 AI provider 配置骨架

- 优先级：P2
- 状态：done
- 所属阶段：Phase 14
- 目标：设计真实 OpenAI-compatible provider 的配置骨架和安全边界，不接入真实密钥。
- 涉及文件/模块：`docs/ARCHITECTURE.md`、`docs/API_DESIGN.md`、`backend/ai`
- 验收标准：文档明确 provider 切换、环境变量、密钥安全和 mock 回退策略；不得提交真实 API Key。
- 验证命令：`python3 scripts/agent_changelog_archive.py`；`python3 scripts/agent_doc_check.py`；`git diff --check`
- 完成后更新：`docs/BACKLOG.md`、`docs/CURRENT_STATE.md`、`docs/CHANGELOG_AGENT.md`、`docs/DECISION_LOG.md`（如确定 provider 策略）
- 是否阻塞后续任务：否
- 完成后建议下一任务：P2-002

### P2-002 CI 增加前端测试步骤

- 优先级：P2
- 状态：done
- 所属阶段：Phase 12
- 目标：CI 前端 job 在 build 之前运行 `npm test`，确保前端测试在每次提交时自动验证。
- 涉及文件/模块：`.github/workflows/ci.yml`
- 验收标准：CI 前端 job 包含 `npm test` 步骤，且位于 `npm run build` 之前。
- 验证命令：`cd frontend && npm test`；`python3 scripts/agent_changelog_archive.py`；`python3 scripts/agent_doc_check.py`
- 完成后更新：`docs/BACKLOG.md`、`docs/CURRENT_STATE.md`、`docs/CHANGELOG_AGENT.md`
- 是否阻塞后续任务：否
- 完成后建议下一任务：P2-003

### P2-003 增加后端 Service 层单元测试

- 优先级：P2
- 状态：done
- 所属阶段：Phase 12
- 目标：为后端关键 Service 层增加独立单元测试，补充现有 Controller 集成测试的覆盖。
- 涉及文件/模块：`backend/src/test/java/com/lifepilot/`（TransactionService、StatisticService、AiService 等）
- 验收标准：至少覆盖 AiService 的 parseTransaction/parseShoppingList/parseTodo 异常路径和 StatisticService 的聚合计算；`./mvnw test` 通过。
- 验证命令：`cd backend && ./mvnw test -B`；`python3 scripts/agent_changelog_archive.py`；`python3 scripts/agent_doc_check.py`
- 完成后更新：`docs/BACKLOG.md`、`docs/CURRENT_STATE.md`、`docs/CHANGELOG_AGENT.md`、`docs/TESTING.md`
- 是否阻塞后续任务：否
- 完成后建议下一任务：P2-004

### P2-004 实现 OpenAI provider 代码骨架

- 优先级：P2
- 状态：done
- 所属阶段：Phase 14
- 目标：基于 P2-001 的配置文档，实现 `OpenAiProvider` 类和 Spring 条件注入配置，包含 HTTP 调用、JSON 反序列化、超时重试和 Mock 回退。
- 涉及文件/模块：`backend/src/main/java/com/lifepilot/ai/`（OpenAiProvider、AiProviderConfig）、`backend/src/main/resources/application.yml`、`backend/src/test/java/com/lifepilot/ai/`
- 验收标准：`provider=mock` 行为不变；`provider=openai` 且 API Key 为空时自动回退并打印警告；`OpenAiProvider` 能用真实 Key 调用但测试用 mock；`./mvnw test` 通过。
- 验证命令：`cd backend && ./mvnw test -B`；`cd frontend && npm run build`；`python3 scripts/agent_changelog_archive.py`；`python3 scripts/agent_doc_check.py`
- 完成后更新：`docs/BACKLOG.md`、`docs/CURRENT_STATE.md`、`docs/CHANGELOG_AGENT.md`、`docs/ARCHITECTURE.md`
- 是否阻塞后续任务：否
- 完成后建议下一任务：P3-001

## P3 Tasks

### P3-001 前端首页统计图表可视化

- 优先级：P3
- 状态：done
- 所属阶段：Phase 15
- 目标：在 HomeView 首页使用 ECharts 渲染统计图表，将后端返回的统计数据以可视化图表形式展示，包括：月度收支柱状图、支出分类饼图、待办状态饼图。
- 涉及文件/模块：`frontend/src/views/HomeView.vue`、`frontend/src/components/EChart.vue`（新建通用图表组件）、`frontend/src/api/statistics.ts`
- 验收标准：首页已登录用户可看到 3 个 ECharts 图表（月度收支概览、支出分类占比、待办状态分布）；图表数据来自后端真实接口；空数据时图表不渲染；`npm run build` 通过；前端测试通过。
- 验证命令：`cd frontend && npm run build`；`cd frontend && npm test`；`python3 scripts/agent_changelog_archive.py`；`python3 scripts/agent_doc_check.py`
- 完成后更新：`docs/BACKLOG.md`、`docs/CURRENT_STATE.md`、`docs/CHANGELOG_AGENT.md`
- 是否阻塞后续任务：否
- 完成后建议下一任务：P3-002

### P3-002 实现分类财务统计接口

- 优先级：P3
- 状态：done
- 所属阶段：Phase 10
- 目标：实现 `GET /api/spaces/{spaceId}/statistics/finance/categories` 接口，支持 `?year=&month=` 参数，返回当月各分类支出/收入汇总。补充 API_DESIGN 中缺失的统计接口。
- 涉及文件/模块：`backend/src/main/java/com/lifepilot/statistics/`（StatisticController、StatisticService、新增 DTO）
- 验收标准：接口正确返回分类汇总数据；无数据时返回空列表；`./mvnw test` 通过。
- 验证命令：`cd backend && ./mvnw test -B`；`python3 scripts/agent_changelog_archive.py`；`python3 scripts/agent_doc_check.py`
- 完成后更新：`docs/BACKLOG.md`、`docs/CURRENT_STATE.md`、`docs/CHANGELOG_AGENT.md`、`docs/API_DESIGN.md`
- 是否阻塞后续任务：否
- 完成后建议下一任务：P3-003

### P3-003 实现购物统计接口

- 优先级：P3
- 状态：done
- 所属阶段：Phase 10
- 目标：实现 `GET /api/spaces/{spaceId}/statistics/shopping` 接口，返回购物清单总数、待采购/已完成清单数、近 30 天清单数量趋势。
- 涉及文件/模块：`backend/src/main/java/com/lifepilot/statistics/`（StatisticController、StatisticService、新增 ShoppingStatsResponse DTO）、`frontend/src/api/statistics.ts`
- 验收标准：接口正确返回购物统计数据；`./mvnw test` 通过。
- 验证命令：`cd backend && ./mvnw test -B`；`python3 scripts/agent_changelog_archive.py`；`python3 scripts/agent_doc_check.py`
- 完成后更新：`docs/BACKLOG.md`、`docs/CURRENT_STATE.md`、`docs/CHANGELOG_AGENT.md`、`docs/API_DESIGN.md`
- 是否阻塞后续任务：否
- 完成后建议下一任务：P3-004

### P3-004 后端 Service 层补充测试

- 优先级：P3
- 状态：done
- 所属阶段：Phase 12
- 目标：为缺少独立 Service 测试的模块补充单元测试，覆盖 TransactionService、ShoppingService、InventoryService、TodoService、RecipeService、DocumentService 的核心业务逻辑和异常路径。
- 涉及文件/模块：`backend/src/test/java/com/lifepilot/`
- 验收标准：每个 Service 至少 3 个测试用例覆盖正常路径和异常路径；`./mvnw test` 通过且测试数量增长。
- 验证命令：`cd backend && ./mvnw test -B`；`python3 scripts/agent_changelog_archive.py`；`python3 scripts/agent_doc_check.py`
- 完成后更新：`docs/BACKLOG.md`、`docs/CURRENT_STATE.md`、`docs/CHANGELOG_AGENT.md`、`docs/TESTING.md`
- 是否阻塞后续任务：否
- 完成后建议下一任务：P3-005

### P3-005 前端 API 层测试补充

- 优先级：P3
- 状态：done
- 所属阶段：Phase 13
- 目标：为前端 API 模块（statistics、shopping、inventory、todo、document、ai）补充测试，验证请求路径、参数传递和响应处理逻辑。
- 涉及文件/模块：`frontend/src/api/__tests__/`
- 验收标准：至少 4 个 API 模块有测试覆盖；`npm test` 通过。
- 验证命令：`cd frontend && npm test`；`cd frontend && npm run build`；`python3 scripts/agent_changelog_archive.py`；`python3 scripts/agent_doc_check.py`
- 完成后更新：`docs/BACKLOG.md`、`docs/CURRENT_STATE.md`、`docs/CHANGELOG_AGENT.md`、`docs/TESTING.md`
- 是否阻塞后续任务：否
- 完成后建议下一任务：P4-001

## P4 Tasks

### P4-001 实现用户个人资料编辑

- 优先级：P4
- 状态：done
- 所属阶段：Phase 16
- 目标：实现 `PUT /api/users/me` 接口，支持修改 displayName 和 avatarUrl；前端新增个人设置页面，可查看和编辑个人信息。
- 涉及文件/模块：`backend/src/main/java/com/lifepilot/user/`（UserService、UserController、新增 DTO）、`frontend/src/views/profile/ProfileView.vue`（新建）、`frontend/src/api/auth.ts`、`frontend/src/router/index.ts`、`frontend/src/layouts/AppShell.vue`
- 验收标准：用户可通过 `PUT /api/users/me` 更新 displayName/avatarUrl；前端个人设置页面可查看当前信息并提交更新；导航栏显示"设置"入口；后端测试通过，前端构建通过。
- 验证命令：`cd backend && ./mvnw test -B`；`cd frontend && npm test`；`cd frontend && npm run build`；`python3 scripts/agent_changelog_archive.py`；`python3 scripts/agent_doc_check.py`
- 完成后更新：`docs/BACKLOG.md`、`docs/CURRENT_STATE.md`、`docs/CHANGELOG_AGENT.md`、`docs/API_DESIGN.md`
- 是否阻塞后续任务：否
- 完成后建议下一任务：P4-002

### P4-002 库存临期和缺货提醒逻辑

- 优先级：P4
- 状态：done
- 所属阶段：Phase 16
- 目标：在 StatisticService 中实现库存临期（7 天内过期）和缺货（数量 ≤ 0 或低于自定义阈值）的提醒查询，前端首页展示提醒列表。
- 涉及文件/模块：`backend/src/main/java/com/lifepilot/statistics/`（StatisticService、StatisticController、新增 DTO）、`frontend/src/api/statistics.ts`、`frontend/src/views/HomeView.vue`
- 验收标准：`GET /api/spaces/{spaceId}/statistics/inventory/alerts` 返回临期和缺货物品列表；前端首页展示提醒列表；后端测试通过。
- 验证命令：`cd backend && ./mvnw test -B`；`cd frontend && npm run build`；`python3 scripts/agent_changelog_archive.py`；`python3 scripts/agent_doc_check.py`
- 完成后更新：`docs/BACKLOG.md`、`docs/CURRENT_STATE.md`、`docs/CHANGELOG_AGENT.md`、`docs/API_DESIGN.md`
- 是否阻塞后续任务：否
- 完成后建议下一任务：P4-003

### P4-003 待办完成率统计接口

- 优先级：P4
- 状态：done
- 所属阶段：Phase 16
- 目标：在 StatisticService 中实现待办完成率、近 30 天完成趋势统计，前端首页展示待办完成率图表。
- 涉及文件/模块：`backend/src/main/java/com/lifepilot/statistics/`（StatisticService、StatisticController、更新 TodoStatsResponse）、`frontend/src/api/statistics.ts`、`frontend/src/views/HomeView.vue`
- 验收标准：`GET /api/spaces/{spaceId}/statistics/todos` 返回完成率和趋势数据；前端展示完成率；后端测试通过。
- 验证命令：`cd backend && ./mvnw test -B`；`cd frontend && npm run build`；`python3 scripts/agent_changelog_archive.py`；`python3 scripts/agent_doc_check.py`
- 完成后更新：`docs/BACKLOG.md`、`docs/CURRENT_STATE.md`、`docs/CHANGELOG_AGENT.md`、`docs/API_DESIGN.md`
- 是否阻塞后续任务：否
- 完成后建议下一任务：P4-004

### P4-004 购物预算估算功能

- 优先级：P4
- 状态：done
- 所属阶段：Phase 16
- 目标：购物清单新增 budgetAmount 字段，用户可设置预算金额；购物清单列表和详情页面展示预算与实际花费对比。
- 涉及文件/模块：`backend/src/main/java/com/lifepilot/shopping/`（ShoppingList、ShoppingService、ShoppingController、DTO）、`backend/src/main/resources/db/migration/V9__add_shopping_budget.sql`（新建）、`frontend/src/api/shopping.ts`、`frontend/src/views/shopping/ShoppingView.vue`
- 验收标准：购物清单可设置预算金额；列表显示预算；后端测试通过；Flyway 迁移通过。
- 验证命令：`cd backend && ./mvnw test -B`；`cd frontend && npm run build`；`python3 scripts/agent_changelog_archive.py`；`python3 scripts/agent_doc_check.py`
- 完成后更新：`docs/BACKLOG.md`、`docs/CURRENT_STATE.md`、`docs/CHANGELOG_AGENT.md`、`docs/API_DESIGN.md`、`docs/DB_DESIGN.md`
- 是否阻塞后续任务：否
- 完成后建议下一任务：P4-005

### P4-005 前端视图层组件测试

- 优先级：P4
- 状态：todo
- 所属阶段：Phase 16
- 目标：为前端关键页面（AuthView、HomeView、FinanceView）增加 Vue 组件测试，覆盖表单提交、状态切换和错误处理交互逻辑。
- 涉及文件/模块：`frontend/src/views/__tests__/`（新建目录）、`frontend/src/test/setup.ts`
- 验收标准：至少 2 个页面有组件测试覆盖；`npm test` 通过。
- 验证命令：`cd frontend && npm test`；`cd frontend && npm run build`；`python3 scripts/agent_changelog_archive.py`；`python3 scripts/agent_doc_check.py`
- 完成后更新：`docs/BACKLOG.md`、`docs/CURRENT_STATE.md`、`docs/CHANGELOG_AGENT.md`、`docs/TESTING.md`
- 是否阻塞后续任务：否
- 完成后建议下一任务：待定
