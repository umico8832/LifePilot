# Backlog

状态值：`todo`、`in_progress`、`done`、`blocked`。

Agent 选择任务时，优先执行最高优先级、最低阶段编号、未阻塞的 `todo` 任务。任务完成后，更新本文件、`docs/CURRENT_STATE.md` 和 `docs/CHANGELOG_AGENT.md`。

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
### P1-011 实现票据与文件管理 CRUD

- 优先级：P1
- 状态：done
- 所属阶段：Phase 11
- 目标：实现票据、收据、保修卡、合同、说明书和证件索引的增删改查，支持按类型筛选和到期提醒。
- 涉及文件/模块：`backend/document`、`backend/src/main/resources/db/migration/V8__create_document_record.sql`、`frontend/src/api/document.ts`、`frontend/src/views/document/DocumentView.vue`、`frontend/src/router/index.ts`、`frontend/src/layouts/AppShell.vue`
- 验收标准：前后端可创建、查看、更新、删除文档记录；支持 invoice/receipt/warranty/contract/manual/certificate/other 类型；支持按类型筛选；即将过期（30天内）标识；导航栏显示文档入口。
- 是否阻塞后续任务：否
- 完成后建议下一任务：P1-012 待定
