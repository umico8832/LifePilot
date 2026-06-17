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
- 状态：todo
- 所属阶段：Phase 12
- 目标：添加后端测试和前端构建 CI。
- 涉及文件/模块：`.github/workflows`
- 验收标准：CI 可运行 Maven test 和 npm build。
- 是否阻塞后续任务：否
- 完成后建议下一任务：P1-003 增加 OpenAPI 文档

### P1-003 增加 OpenAPI 文档

- 优先级：P1
- 状态：todo
- 所属阶段：Phase 2
- 目标：接入 springdoc 或 Knife4j。
- 涉及文件/模块：`backend/config`
- 验收标准：本地可访问 API 文档页面。
- 是否阻塞后续任务：否
- 完成后建议下一任务：P1-004 完善前端空态和错误态
