# AI 使用说明

## 1. 使用的 AI 工具

| 工具 | 用途 |
|------|------|
| OpenCode (Sisyphus) | 全流程：需求分析、架构设计、代码生成、测试、文档 |

## 2. 使用场景

| 阶段 | 是否使用 AI | 说明 |
|------|------------|------|
| 需求理解 | 是 | AI 辅助分析业务场景、识别核心对象和业务规则 |
| 任务拆解 | 是 | AI 辅助将需求拆解为 9 阶段可执行的开发任务 |
| 方案设计 | 是 | AI 辅助技术栈选择、工程结构和关键模块设计；用户纠正：ORM 从 JPA 切换为 MyBatis |
| 代码生成 | 是 | AI 辅助生成实体、Mapper(接口+XML)、Service、Controller、前端页面（全部 36 个 Java 文件 + 8 个资源文件） |
| 测试生成 | 是 | AI 辅助生成 4 个测试类共 22 个测试用例 + DeepSeek 集成测试 6 个 |
| Bug 修复 | 是 | AI 辅助修复 H2 自增序列与种子数据冲突（DuplicateKey 问题） |
| 文档编写 | 是 | AI 辅助生成 API 设计说明、测试计划、过程记录 |
| DeepSeek 集成 | 是 | AI 辅助实现 DeepSeek API 调用、配置、测试，替换 Mock 实现为真实 AI

## 3. 关键 Prompt / Skill 摘要

### 3.1 项目上下文分析
```
分析项目需求，生成项目分析报告
```
→ AI 通过 glob 扫描全项目文件、读取考题说明和设计文档，生成完整的项目分析报告（文件清单、完成度矩阵、设计决策矩阵、风险点）。

### 3.2 执行计划生成
```
根据项目需求分析结果生成执行计划
```
→ AI 基于已完成的 process-record.md 设计，生成 9 阶段详细执行计划（.omo/plans/execution-plan.md），包含每阶段的产出物、工时、依赖关系和验收标准。

### 3.3 核心业务逻辑生成
```
委托子代理创建 StateMachine + Services + Controllers + 异常处理
```
→ AI 通过 general 子代理在后台并行生成 9 个核心 Java 文件（191 秒完成），包括状态机引擎（含阻断规则）、3 个 Service、3 个 Controller、异常处理。

### 3.4 测试生成
```
创建 4 个测试类 covering 状态机/工作项/澄清/AI
```
→ AI 直接生成 4 个测试类共 22 个测试用例，覆盖全部核心业务规则验证场景。

### 3.5 文档完善
```
完善 4 份交付文档（api-design / test-plan / ai-usage / process-record）
```
→ AI 基于执行计划和实际代码实现，填充完整的 API 设计说明、测试计划和使用记录。

### 3.6 DeepSeek API 集成
```
接入 deepseek 官方 api，做真实 ai 分析
```
→ AI 综合分析现有 AI 层架构（AIAnalysisService 接口 + Mock 实现 + @Profile 切换机制），创建：
- `DeepSeekProperties.java` — 配置属性类（api-key, api-url, model, timeout）
- `DeepSeekAIAnalysisService.java` — 真实 API 调用实现，使用 RestTemplate + Jackson，支持 JSON mode 和 markdown 代码块解析
- `DeepSeekAIAnalysisServiceTest.java` — 6 个 Mockito 测试覆盖：正常调用、markdown 解析、密钥校验、空响应、超时异常、解析 fallback
- 更新 ErrorCode 添加 EXTERNAL_SERVICE_ERROR；application.yml 添加 deepseek 配置段
- 使用 `@Profile("llm")` 实现与 Mock 实现的零冲突切换

## 4. AI 生成内容

### 4.1 后端 Java 源代码（39 个文件）
- **枚举** (5): WorkItemStatus, WorkItemType, Priority, QuestionSeverity, QuestionStatus
- **实体** (3): WorkItem, ClarificationQuestion, StatusTransition
- **Mapper 接口** (3): WorkItemMapper, ClarificationQuestionMapper, StatusTransitionMapper
- **DTO** (10): 原有 9 个 + AIAnalysisResult
- **Service** (5): StateMachineService, WorkItemService, ClarificationQuestionService, MockAIAnalysisService, **DeepSeekAIAnalysisService**
- **配置** (1): **DeepSeekProperties**
- **接口** (1): AIAnalysisService
- **Controller** (5): WorkItemController, TransitionController, ClarificationController, AIAnalysisController, PageController
- **异常** (3): BusinessException, ErrorResponse, GlobalExceptionHandler（ErrorCode 新增 EXTERNAL_SERVICE_ERROR）
- **入口** (1): Application.java, WebConfig.java

### 4.2 Mapper XML（3 个）
WorkItemMapper.xml, ClarificationQuestionMapper.xml, StatusTransitionMapper.xml

### 4.3 资源文件（7 个）
pom.xml, application.yml, schema.sql, data.sql, index.html, detail.html, style.css, main.js

### 4.4 测试代码（5 个）
StateMachineServiceTest (7 tests), WorkItemServiceTest (5 tests), ClarificationServiceTest (6 tests), MockAIAnalysisServiceTest (4 tests), **DeepSeekAIAnalysisServiceTest (6 tests)**

### 4.5 执行计划
.omo/plans/execution-plan.md — 9 阶段 308 行详细执行计划

### 4.6 交付文档
process-record.md, ai-usage-record.md, api-design-proposal.md, test-plan.md

## 5. 人工修正内容

### 5.1 ORM 技术选型纠正
- **AI 输出问题**: 初始设计使用 MyBatis
- **修正方式**: 用户明确指出使用 Spring Data JPA + Hibernate，AI 同步更新了 process-record.md（技术栈、工程结构）、execution-plan.md（实体设计、Repository、schema.sql DDL 替代 Mapper）和 ai-usage-record.md（标注纠正记录）
- **影响范围**: 实体从纯POJO 改为注解 JPA，Mapper + XML改为Repository 

### 5.2 H2 自增序列冲突
- **AI 输出问题**: data.sql 使用显式 ID (1, 2) 插入种子数据，导致后续 H2 自增序列从 1 开始，与已有记录冲突
- **修正方式**: 在 data.sql 末尾添加 `ALTER TABLE ... ALTER COLUMN id RESTART WITH 100` 重置序列
- **结果**: 22 个测试全部通过

## 6. 效果评价

### ✅ 有效的方面
- **需求分析和设计阶段**：AI 在项目上下文理解、任务拆解、技术方案设计方面表现优秀，process-record.md 从模板快速填充为完整的设计文档
- **代码生成**：36 个 Java 文件的代码结构一致、包命名规范、接口设计合理，BUILD SUCCESS 一次通过
- **测试生成**：22 个测试用例覆盖了全部核心业务规则，测试通过率 100%
- **文档生成**：AI 能基于实际代码生成准确的 API 列表和测试覆盖矩阵

### ⚠️ 需要人工介入的方面
- **技术选型**：AI 默认使用 MyBatis，需人工纠正为 JPA
- **边界问题**：H2 序列冲突问题需人工定位和修复
- **前端页面**：AI 生成的 HTML/JS 为演示用途，UI 较简陋，不适合生产使用

### 整体评价
AI 在编码和文档阶段帮助显著，将大量重复性工作（实体定义、Mapper XML、Controller 模板、测试模板、真实 API 集成）自动化，开发效率提升明显。关键决策（技术栈、架构设计）仍需人工把关。

## 7. DeepSeek 集成补充说明

### 7.1 架构设计
DeepSeek 集成遵循原有的策略模式：
- `AIAnalysisService` 接口定义分析契约（不变）
- `MockAIAnalysisService`（`@Profile("!llm")`）— 默认激活，基于关键词规则
- `DeepSeekAIAnalysisService`（`@Profile("llm")`）— 通过 `--spring.profiles.active=llm` 激活

### 7.2 关键设计决策
| 决策 | 选择 | 理由 |
|------|------|------|
| HTTP 客户端 | RestTemplate（已有依赖） | spring-boot-starter-web 已内置，无需额外依赖 |
| API 格式 | OpenAI 兼容格式 | DeepSeek 官方 API 兼容 OpenAI，方便未来切换其他 LLM |
| JSON mode | `response_format: {"type": "json_object"}` | 确保 AI 返回结构化 JSON，减少解析失败 |
| 错误处理 | BusinessException + fallback | API 超时/失败时用户看到友好提示，部分解析失败有降级结果 |
| API 密钥 | 环境变量 `DEEPSEEK_API_KEY` | 避免密钥硬编码在配置文件中 |

### 7.3 使用方式
```bash
# 设置 API 密钥
export DEEPSEEK_API_KEY=sk-your-key-here

# 使用 DeepSeek 启动
mvn spring-boot:run -Dspring-boot.run.profiles=llm

# 验证
curl http://localhost:8080/  # 前端页面
curl http://localhost:8080/swagger-ui.html  # Swagger 文档
```
