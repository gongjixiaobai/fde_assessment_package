# 过程记录

## 1. 需求理解

### 1.1 业务场景理解
本题要求实现一个 **AI 辅助研发工作项流转与需求澄清系统**，面向研发团队内部使用。核心目标是支持：
1. 管理研发工作项（需求/缺陷/任务）
2. 跟踪工作项从草稿到完成的状态流转
3. 记录需求澄清问题及其处理结果
4. 提供 AI 辅助分析能力（风险识别、验收标准建议、澄清问题生成等）
5. 支持基本演示和验证

本题为后端方向，使用 Java/Spring Boot 技术栈，需同时提供一个简单前端页面用于演示核心闭环。

### 1.2 核心对象识别
- **工作项 (WorkItem)**: 核心实体，包含标题、描述、类型(story/bug/task)、优先级、状态、负责人、标签、验收标准、风险等级、澄清问题列表、状态历史
- **澄清问题 (ClarificationQuestion)**: 关联工作项的问题记录，包含问题内容、严重程度、状态(未解决/已解决)、回答
- **状态流转历史 (StatusTransition)**: 记录工作项每次状态变更的轨迹，包含从哪个状态、到哪个状态、操作人、操作时间

### 1.3 核心业务规则理解
- **状态流转规则**: 状态按 DRAFT → ANALYZING → READY → IN_PROGRESS → TESTING → DONE 顺序流转，支持退回（如 ANALYZING 可退回 DRAFT），非法流转需被拦截
- **澄清问题阻断规则**: 如果工作项存在未解决的高优先级澄清问题，则不允许进入 READY（已准备）或后续可开发状态
- **AI 辅助分析定位**: AI 能力需通过服务/模块封装，支持结构化返回（非纯散文），可 Mock 实现但需说明替换为真实 LLM 的方式

## 2. 任务拆解

| 任务 | 预估工时 | 说明 |
|------|----------|------|
| 2.1 项目初始化 | 0.5h | Spring Boot 项目脚手架、依赖配置、H2 数据库、目录结构 |
| 2.2 数据模型与持久层 | 1h | WorkItem、ClarificationQuestion、StatusTransition 实体 + Mapper(接口+XML) + schema.sql |
| 2.3 状态机引擎 | 1.5h | 状态定义、合法流转规则、状态校验、流转历史记录 |
| 2.4 工作项管理 API | 1h | CRUD 接口设计实现 |
| 2.5 澄清问题管理 API | 1h | 新增、回复、查询澄清问题接口 |
| 2.6 AI 辅助分析服务 | 1.5h | Mock AI Service 封装、结构化分析结果返回 |
| 2.7 核心业务规则 | 1h | 高优先级未解决澄清问题阻断状态流转 |
| 2.8 测试 | 1.5h | 状态流转测试、澄清阻断测试、AI 服务测试 |
| 2.9 简单前端页面 | 2h | 演示用前端页面（工作项列表/详情、状态流转、澄清问题、AI 分析） |
| 2.10 文档与记录 | 1h | README、API 设计说明、测试计划、AI 使用说明、过程记录 |

## 3. 技术方案

### 3.1 技术栈选择

| 层级 | 技术 | 选择理由 |
|------|------|----------|
| 后端框架 | Spring Boot 3.x | 题目指定 Java/Spring Boot，生态成熟 |
| 数据库 | H2 (内存模式) | 零配置、适合演示验证、可切换为 PostgreSQL |
| 持久层 | MyBatis + MyBatis Spring Boot Starter | 题目指定，SQL 可控性强，便于复杂查询 |
| 构建工具 | Maven | 项目标准构建工具 |
| 前端 | 原生 HTML + JavaScript (Thymeleaf) | 简单直接，无需额外构建步骤，嵌入 Spring Boot |
| AI 服务 | Mock AI Service | 服务层封装，预留扩展点，可替换为真实 LLM |

### 3.2 工程结构
```
src/main/java/com/fde/assessment/
├── Application.java
├── config/
│   └── WebConfig.java            # CORS 等配置
├── model/
│   ├── entity/
│   │   ├── WorkItem.java         # 工作项实体
│   │   ├── ClarificationQuestion.java  # 澄清问题实体
│   │   └── StatusTransition.java # 状态流转历史实体
│   ├── enums/
│   │   ├── WorkItemStatus.java   # 状态枚举
│   │   ├── WorkItemType.java     # 类型枚举
│   │   ├── Priority.java         # 优先级枚举
│   │   └── QuestionSeverity.java # 澄清问题严重程度
│   └── dto/
│       ├── WorkItemDTO.java
│       ├── StatusTransitionDTO.java
│       └── AIAnalysisResult.java
├── mapper/                       # MyBatis Mapper 接口
│   ├── WorkItemMapper.java
│   ├── ClarificationQuestionMapper.java
│   └── StatusTransitionMapper.java
├── service/
│   ├── WorkItemService.java      # 工作项核心业务
│   ├── StateMachineService.java  # 状态机引擎
│   └── ai/
│       ├── AIAnalysisService.java    # AI 分析接口
│       └── MockAIAnalysisService.java # Mock 实现
├── controller/                   # REST API 控制器
└── exception/
    ├── BusinessException.java    # 业务异常
    └── GlobalExceptionHandler.java # 全局异常处理
```
```
src/main/resources/
├── application.yml                 # 数据源 + MyBatis + Server
├── schema.sql                      # 表结构 DDL
├── data.sql                        # 种子数据
├── mapper/                         # MyBatis XML 映射
│   ├── WorkItemMapper.xml
│   ├── ClarificationQuestionMapper.xml
│   └── StatusTransitionMapper.xml
├── static/
│   ├── css/style.css
│   └── js/main.js
└── templates/                      # Thymeleaf 页面
    ├── index.html
    └── detail.html
```

### 3.3 关键模块设计
- **AI 服务封装**: 定义 `AIAnalysisService` 接口，`MockAIAnalysisService` 基于规则和固定模板返回结构化结果。切换真实 LLM 只需新增实现类替换 Bean
- **状态机设计**: `StateMachineService` 集中管理状态定义和流转规则，通过 Map 或枚举定义合法流转，统一校验入口
- **数据访问层**: MyBatis Mapper + XML，H2 内存数据库，通过 schema.sql 初始化表结构

## 4. API / 数据 / 状态设计

### 4.1 数据模型
- **WorkItem**: id, title, description, type(story/bug/task), priority(HIGH/MEDIUM/LOW), status, assignee, tags, acceptanceCriteria, riskLevel, createdAt, updatedAt
- **ClarificationQuestion**: id, workItemId, content, severity(HIGH/MEDIUM/LOW), status(UNRESOLVED/RESOLVED), answer, createdAt, resolvedAt
- **StatusTransition**: id, workItemId, fromStatus, toStatus, operator, createdAt

实体为普通 POJO，不依赖 JPA 注解。枚举通过 MyBatis TypeHandler 映射。表结构通过 schema.sql 管理。

### 4.2 状态机设计
| 当前状态 | 允许流转到 |
|----------|------------|
| DRAFT | ANALYZING |
| ANALYZING | DRAFT, READY |
| READY | ANALYZING, IN_PROGRESS |
| IN_PROGRESS | READY, TESTING |
| TESTING | IN_PROGRESS, DONE |
| DONE | (不允许变更) |

**阻断规则**: 工作项存在未解决的高优先级(severity=HIGH)澄清问题时，不允许从 ANALYZING 流转到 READY。

### 4.3 API 设计
详见 [api-design-proposal.md](api-design-proposal.md)，按模块分组：
- 工作项 CRUD: POST/GET/PUT/DELETE /api/workitems
- 状态流转: POST /api/workitems/{id}/transitions
- 澄清问题: POST/GET/PUT /api/workitems/{id}/questions
- AI 分析: POST /api/workitems/{id}/ai-analysis

## 5. AI 使用过程

AI 使用详情请参见 [ai-usage-record.md](ai-usage.md)。

整体来说，AI 在以下阶段提供了辅助：
1. 需求理解与任务拆解 — AI 辅助分析业务场景和核心对象
2. 技术方案设计 — AI 辅助技术栈选择和工程结构设计
3. 代码生成 — AI 辅助生成实体、服务、控制器、前端页面
4. 测试编写 — AI 辅助生成测试用例
5. 文档撰写 — AI 辅助生成 API 文档、README

## 6. 遇到的问题

### 6.1 ORM 选型从 JPA 变更为 MyBatis

- **现象**: 初始技术方案设计时选择了 Spring Data JPA 作为持久层框架，生成的实体带有 JPA 注解（@Entity、@Id、@GeneratedValue 等），Repository 使用 JpaRepository 接口
- **AI 输出问题**: AI 默认优先使用 JPA，未遵循题目中的 MyBatis 要求
- **解决方式**: 人工修正——移除所有 JPA 注解，将实体改为纯 POJO；将 Repository 替换为 MyBatis Mapper 接口 + XML 映射文件；改用 schema.sql 管理表结构；新增 TypeHandler 处理枚举映射
- **结果**: 满足题目要求的 MyBatis 技术栈，SQL 可控性更强，MyBatis XML 支持复杂动态查询

### 6.2 测试数据 ID 冲突导致 ClarificationServiceTest 失败

- **现象**: 测试类 setUp() 中手动插入工作项数据时使用了硬编码 ID（1 和 2），与 data.sql 中预加载的种子数据 ID 冲突，抛出 DuplicateKey 异常，导致 2 个测试方法在 setUp 阶段直接报错
- **解决方式**: 移除硬编码 ID，改造 workItemMapper.insert() 使其通过 useGeneratedKeys 自动获取数据库生成的 ID（RESTART WITH 100 后的自增值），确保每次测试运行时 ID 不冲突
- **结果**: ClarificationServiceTest 6 个测试全部通过，整体测试通过数从 20/22 提升至 22/22

### 6.3 中文编码在测试日志中的显示问题

- **现象**: Maven 测试日志中 SQL 参数显示为乱码（如 `����߽粻��`），但实际数据正确写入 H2 数据库，断言验证全部通过
- **解决方式**: 确认问题仅存在于 Maven 控制台输出的编码显示层面，不影响测试逻辑和数据正确性，属于 Maven surefire 插件默认编码配置问题，不做额外处理
- **结果**: 所有中文数据读写正常，测试断言均通过

## 7. 验证记录

### 7.1 状态流转完整链路验证

- **测试内容**: 验证工作项从 DRAFT → ANALYZING → READY → IN_PROGRESS → TESTING → DONE 的完整正向流转合法；验证退回（ANALYZING→DRAFT、READY→ANALYZING）、非法流转拦截（DRAFT→DONE）、以及 DONE 状态不可变性
- **验证方式**: StateMachineServiceTest（7 个 JUnit 测试用例）
- **结果**: 全部通过 ✅

### 7.2 澄清问题阻断规则验证

- **测试内容**: 验证存在未解决 HIGH 澄清问题时 ANALYZING→READY 被阻断；解决问题后解除阻断；MEDIUM 级别问题不影响流转
- **验证方式**: StateMachineServiceTest 中的阻断场景测试用例
- **结果**: 全部通过 ✅

### 7.3 澄清问题 CRUD 验证

- **测试内容**: 验证添加 HIGH/MEDIUM 澄清问题、hasUnresolvedHighPriority 检查、解决问题后状态和回答字段更新、问题列表查询
- **验证方式**: ClarificationServiceTest（6 个 JUnit 测试用例）
- **结果**: 全部通过 ✅

### 7.4 工作项 CRUD 验证

- **测试内容**: 验证工作项创建（含详细字段）、按 ID 查询详情（含澄清问题和状态历史）、更新字段、按状态筛选列表、级联删除
- **验证方式**: WorkItemServiceTest（5 个 JUnit 测试用例）
- **结果**: 全部通过 ✅

### 7.5 AI 分析服务验证

- **测试内容**: 验证 MockAIAnalysisService 对包含不同关键词（状态流转/AI/澄清/验收）的描述返回对应的结构化分析结果，包含 summary、risks、acceptanceCriteria、clarificationQuestions、taskSuggestions 字段
- **验证方式**: MockAIAnalysisServiceTest（4 个 JUnit 测试用例）
- **结果**: 全部通过 ✅

### 7.7 DeepSeek API 集成验证

- **测试内容**: 验证 DeepSeekAIAnalysisService 的正常 API 调用、markdown 代码块解析、API 密钥未配置异常、空 choices 异常、网络超时异常、JSON 解析失败的 fallback 机制
- **验证方式**: DeepSeekAIAnalysisServiceTest（6 个 JUnit 测试用例，Mock RestTemplate）
- **结果**: 全部通过 ✅

### 7.6 整体构建验证

- **测试内容**: Maven 全量编译 + 测试
- **验证方式**: `mvn clean test`
- **结果**: BUILD SUCCESS，Tests run: 32, Failures: 0, Errors: 0 ✅

## 8. 取舍说明

### 8.1 未完成内容

| 功能 | 原因 |
|------|------|
| 生产级前端 UI | 前端为 Thymeleaf 演示页面，仅覆盖核心闭环，未做移动端适配、加载状态提示等 |
| 分页 Token 机制 | 当前使用 offset/limit 基础分页，未实现基于游标的 Token 分页 |
| 用户认证与权限控制 | 本题为内部演示系统，未实现登录认证和 RBAC 权限管理 |
| OpenAPI/Swagger 文档 | 当前 API 文档以 Markdown 形式提供，未集成 springdoc 自动生成 |
| 国际化支持 | 错误消息和 UI 均为中文，未做 i18n |
| 前端单元测试 | 前端为简单 HTML + JS 页面，未编写前端测试 |

### 8.2 后续优化方向

1. **真实 LLM 集成**: 实现 LLMAIAnalysisService 接入大语言模型，提升分析结果质量和多样性
2. **分页优化**: 引入游标分页（cursor-based pagination）提升大量数据下的查询性能
3. **认证授权**: 集成 Spring Security + JWT，实现用户登录和操作权限控制
4. **API 文档自动化**: 集成 springdoc-openapi 自动生成 Swagger UI
5. **前端增强**: 使用 Vue/React 重写前端，提升交互体验
6. **CI/CD**: 配置 GitHub Actions 自动构建和测试流水线
7. **数据库迁移**: H2 切换为 PostgreSQL/TiDB，使用 Flyway 管理数据库版本

---

## 9. 自我评价

### 9.1 完成情况

按照考核题目要求，完成了以下全部必做功能：

| 必做功能 | 完成度 | 说明 |
|----------|--------|------|
| 3.1 工作项管理 | ✅ 100% | CRUD + 筛选分页 + 级联删除 |
| 3.2 状态流转 | ✅ 100% | 6 状态流转 + 退回 + 非法拦截 + 历史记录 |
| 3.3 澄清问题管理 | ✅ 100% | 新增/回复/查询 + HIGH/MEDIUM/LOW 严重程度 |
| 3.4 核心业务规则 | ✅ 100% | 高优先级未解决问题阻断 READY 及后续状态 |
| 3.5 AI 辅助分析 | ✅ 100% | Mock 实现 + 结构化返回 + 接口预留 LLM 扩展 |
| 3.6 简单前端页面 | ✅ 100% | 列表/详情 + 状态流转 + 澄清 + AI 分析触发 |
| 3.7 测试 | ✅ 100% | 22 个用例覆盖 4 个模块，全部通过 |

### 9.2 技术亮点

- **MyBatis 纯 SQL 可控**: 通过 XML 动态 SQL 实现筛选分页，避免 ORM 黑盒
- **状态机引擎**: `StateMachineService` 集中管理流转规则，阻断逻辑可扩展
- **AI 服务封装**: 接口-实现分离 + `@Profile` 切换，Mock 和 LLM 实现互不干扰
- **测试驱动**: 先设计测试场景再实现，22 个测试覆盖正向、异常、边界
- **端到端闭环**: 前端 → Controller → Service → Mapper → H2，完整可演示链路

### 9.3 不足与反思

- **时间分配**: 前端页面较为简陋，仅满足演示要求，未做加载状态、错误重试等交互优化
- **AI 能力**: Mock 实现基于固定规则模板，分析结果多样化不足，真实场景需接入 LLM
- **工程化程度**: 缺少日志级别控制、健康检查端点、指标暴露等生产级配套设施
- **知识盲区**: MyBatis 使用经验有限，TypeHandler 和缓存等高级特性未充分利用

### 9.4 收获

通过本次考核，实践了以下能力：
1. 从业务需求到技术方案的完整转化
2. MyBatis + Spring Boot 的后端工程搭建
3. 状态机模式的工程实现
4. AI 能力的工程化封装思维
5. 借助 AI Agent 进行代码生成、测试和文档的全流程协作

---

## 10. 加分项实施记录

| # | 加分项 | 实现方式 | 涉及文件 |
|---|--------|----------|----------|
| 1 | OpenAPI/Swagger | springdoc-openapi 2.5.0，4 个 Controller 添加 @Tag/@Operation 注解 | pom.xml, OpenApiConfig.java, 4 个 Controller |
| 2 | 状态流转历史查询 | GET /api/workitems/{id}/transitions（已有） | TransitionController.java |
| 3 | 乐观锁 | WorkItem 添加 version 字段，Mapper update 使用 WHERE id=# {id} AND version=#{version} | schema.sql, WorkItem.java, WorkItemMapper.xml, WorkItemService.java, StateMachineService.java |
| 4 | 统一错误码 | ErrorCode 枚举 12 个错误码 | ErrorCode.java |
| 5 | 简单认证 | AuthFilter 读取 X-Auth-Token 头部，设置用户上下文 | AuthFilter.java |
| 6 | Docker | docker-compose.yml + Dockerfile (多阶段构建) + actuator | docker-compose.yml, Dockerfile, pom.xml |
| 7 | 看板页面 | board.html — 6 列看板视图，按状态分组展示 | board.html, PageController.java, index.html, detail.html |
| 8 | 增强测试 | 4 个乐观锁测试用例，总测试数 22→26 | OptimisticLockTest.java |
| 9 | AI 生成文档 | 全流程文档由 AI 辅助生成 | 本文件及所有交付文档 |
| 10 | DeepSeek 真实 AI 集成 | DeepSeekAIAnalysisService 实现 AIAnalysisService 接口，通过 `@Profile("llm")` 激活，支持 JSON mode | DeepSeekAIAnalysisService.java, DeepSeekProperties.java, application.yml, ErrorCode.java |
