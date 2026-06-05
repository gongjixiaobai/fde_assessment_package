# AI 辅助研发工作项流转与需求澄清系统

## 1. 题目方向

**后端方向（Java / Spring Boot）**

## 2. 功能清单

| 功能 | 说明 |
|------|------|
| 工作项管理 | 创建、查询（支持筛选分页）、更新、删除工作项 |
| 状态流转 | 6 状态（DRAFT→ANALYZING→READY→IN_PROGRESS→TESTING→DONE），含退回、非法拦截、流转历史 |
| 澄清问题管理 | 新增、回复、查询澄清问题，支持 HIGH/MEDIUM/LOW 严重程度 |
| 核心业务规则 | 高优先级未解决澄清问题阻断状态流转（→READY/IN_PROGRESS/TESTING/DONE） |
| AI 辅助分析 | Mock AI 服务（默认）+ DeepSeek 真实 API（需配置密钥），结构化返回摘要、风险点、验收标准建议、澄清问题建议、任务拆解建议 |
| 前端演示页面 | 工作项列表 + 详情页（含状态流转、澄清问题、AI 分析触发和结果展示） |
| 测试覆盖 | 32 个测试用例，覆盖状态机、CRUD、澄清阻断、AI 分析、乐观锁、DeepSeek 集成 |

## 3. 技术栈

| 层级 | 技术 |
|------|------|
| 后端框架 | Spring Boot 3.2.5 |
| 持久层 | Spring Data JPA |
| 数据库 | H2 内存数据库 |
| 前端 | Thymeleaf + 原生 HTML/CSS/JavaScript |
| 构建工具 | Maven |
| Java 版本 | 17 |

## 4. 如何运行

```bash
# 1. 确保已安装 JDK 17+ 和 Maven 3.6+

# 2. 编译并启动
mvn clean spring-boot:run

# 3. 访问
#    - 前端页面: http://localhost:8080/
#    - H2 控制台: http://localhost:8080/h2-console
#      (JDBC URL: jdbc:h2:mem:assessment, 用户名: sa, 密码留空)
```

## 5. 如何测试

```bash
# 运行全部测试
mvn test

# 预期结果: Tests run: 32, Failures: 0, Errors: 0, Skipped: 0
```

测试覆盖：

| 测试类 | 测试数 | 覆盖内容 |
|--------|--------|----------|
| StateMachineServiceTest | 7 | 状态流转、非法流转、阻断规则、终态 |
| WorkItemServiceTest | 5 | 创建、查询、更新、筛选、删除 |
| ClarificationServiceTest | 6 | 问题 CRUD、HIGH 检测、优先级区分、问题解决 |
| MockAIAnalysisServiceTest | 4 | 结构化返回、关键词匹配、字段完整性 |
| OptimisticLockTest | 4 | 乐观锁版本递增、并发冲突检测 |
| DeepSeekAIAnalysisServiceTest | 6 | 正常调用、markdown 解析、密钥校验、空响应、超时异常、解析 fallback |

**总计: 32 个测试，全部通过 ✅**

## 6. 核心设计说明

### 状态机

```
DRAFT ←→ ANALYZING ←→ READY ←→ IN_PROGRESS ←→ TESTING ──→ DONE(终态)
                  ↑ 阻断点                            ↑ 阻断点
           HIGH 未解决澄清问题                       HIGH 未解决澄清问题
```

### API 设计

RESTful 风格，4 个模块 12 个端点，详见 [api-design-proposal.md](api-design-proposal.md)。

### AI 服务封装

`AIAnalysisService` 接口 + 两种实现通过 `@Profile` 切换：
- `MockAIAnalysisService`（`@Profile("!llm")`）— 默认激活，基于关键词规则
- `DeepSeekAIAnalysisService`（`@Profile("llm")`）— 真实 DeepSeek API

```bash
# 使用 DeepSeek 真实 API 启动：
export DEEPSEEK_API_KEY=sk-your-key-here
mvn spring-boot:run -Dspring-boot.run.profiles=llm

# 或 Windows：
set DEEPSEEK_API_KEY=sk-your-key-here
mvn spring-boot:run -Dspring-boot.run.profiles=llm
```

### 工程结构

```
src/main/java/com/fde/assessment/
├── Application.java, WebConfig.java
├── model/entity/ (WorkItem, ClarificationQuestion, StatusTransition)
├── model/enums/  (WorkItemStatus, WorkItemType, Priority, QuestionSeverity, QuestionStatus)
├── model/dto/    (10 个 Request/Response DTO)
├── repository/   (3 个 JPA Repository 接口)
├── service/      (StateMachineService, WorkItemService, ClarificationQuestionService)
├── service/ai/   (AIAnalysisService 接口, MockAIAnalysisService)
├── controller/   (5 个 REST Controller)
└── exception/    (BusinessException, ErrorResponse, GlobalExceptionHandler)
```

## 7. 已完成内容

### 必做功能
- ✅ Spring Boot 3.2.5 + JPA 项目脚手架
- ✅ 3 个实体 + 5 个枚举 + 3 个 JPA Repository 接口
- ✅ 6 状态状态机引擎（含阻断规则）
- ✅ 工作项 CRUD + 澄清问题管理
- ✅ AI 分析服务（Mock 默认 + DeepSeek 真实 API）
- ✅ 前端页面（列表/详情/看板/流转/澄清/AI 分析）
- ✅ 32 个测试用例全部通过，BUILD SUCCESS
- ✅ 4 份交付文档完整

### 加分功能
- ✅ OpenAPI 3.0 / Swagger UI（springdoc-openapi, `/swagger-ui.html`）
- ✅ 状态流转历史查询
- ✅ 并发更新保护 / 乐观锁（version 字段）
- ✅ 统一错误码体系（ErrorCode 枚举）
- ✅ 简单认证过滤器（X-Auth-Token 头部）
- ✅ Docker Compose + Dockerfile
- ✅ 看板页面（board.html）
- ✅ 增强测试覆盖（乐观锁冲突测试）
- ✅ AI Agent 生成过程文档
- ✅ DeepSeek 真实 AI 集成（官方 API，`@Profile("llm")` 切换）

## 8. 未完成内容及原因

| 功能 | 原因 |
|------|------|
| 生产级前端 UI | 前端为 Thymeleaf 演示页面，仅覆盖核心闭环，未做移动端适配、加载状态提示等 |

## 9. AI 使用说明

详见 [ai-usage-record.md](ai-usage-record.md)。

使用 OpenCode (Sisyphus) 全流程辅助：需求分析 → 架构设计 → 代码生成 → 测试（32 个用例）→ 文档 → 加分项实施。

## 10. Docker 部署

```bash
# 构建并启动
docker-compose up -d

# 停止
docker-compose down

# 访问
# - 应用: http://localhost:8080/
# - Swagger: http://localhost:8080/swagger-ui.html
# - 健康检查: http://localhost:8080/actuator/health
```

