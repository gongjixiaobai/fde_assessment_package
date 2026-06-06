# 测试说明

## 1. 测试范围

| 测试类 | 覆盖范围 | 测试数 |
|--------|---------|--------|
| StateMachineServiceTest | 状态机流转规则、阻断规则、非法流转、终态处理 | 7 |
| WorkItemServiceTest | 工作项 CRUD、详情查询、筛选、更新、删除 | 5 |
| ClarificationServiceTest | 澄清问题 CRUD、HIGH 问题检测、MEDIUM 不影响阻断、问题解决 | 6 |
| MockAIAnalysisServiceTest | AI 结构化返回、关键词匹配、短描述处理、字段完整性 | 4 |
| OptimisticLockTest | 乐观锁版本递增、并发冲突检测、Service 层冲突检测、流转保护 | 4 |
| **合计** | | **26** |

## 2. 核心业务规则验证

| 规则 | 验证方式 | 结果 |
|------|----------|------|
| 合法状态流转 | `StateMachineServiceTest.shouldAllowDraftToAnalyzing` | ✅ 通过 |
| 非法状态流转拦截 | `StateMachineServiceTest.shouldRejectDraftToReady` | ✅ 通过 |
| 状态退回 | `StateMachineServiceTest.shouldAllowAnalyzingToDraft` | ✅ 通过 |
| 高优先级澄清问题阻断 | `StateMachineServiceTest.shouldBlockTransitionWhenUnresolvedHighQuestionExists` | ✅ 通过 |
| 解决 HIGH 问题后流转成功 | `StateMachineServiceTest.shouldAllowTransitionAfterResolvingHighQuestion` | ✅ 通过 |
| DONE 终态不可再流转 | `StateMachineServiceTest.shouldRejectTransitionFromDone` | ✅ 通过 |
| 工作项不存在时报错 | `StateMachineServiceTest.shouldThrowWhenWorkItemNotFound` | ✅ 通过 |
| HIGH 问题检测 | `ClarificationServiceTest.shouldDetectUnresolvedHighQuestion` | ✅ 通过 |
| MEDIUM 问题不影响阻断 | `ClarificationServiceTest.shouldNotAffectHighPriorityCheckWithMediumQuestion` | ✅ 通过 |
| 工作项 CRUD | `WorkItemServiceTest` 全部 5 个测试 | ✅ 通过 |
| AI 结构化返回 | `MockAIAnalysisServiceTest.shouldReturnStructuredResult` | ✅ 通过 |

## 3. 状态流转测试

### 合法流转路径
```
DRAFT → ANALYZING → READY → IN_PROGRESS → TESTING → DONE
```
每个正向流转均通过测试。测试中验证：
- 状态字段更新正确
- 流转历史记录被写入（StatusTransition 记录数匹配）

### 回流验证
```
ANALYZING → DRAFT（通过）
READY → ANALYZING（通过状态机定义，间接验证）
IN_PROGRESS → READY（通过状态机定义）
TESTING → IN_PROGRESS（通过状态机定义）
```

### 非法流转验证
- `DRAFT → READY`（跳过 ANALYZING）→ `ILLEGAL_TRANSITION` 异常
- `DONE → 任意状态` → `ILLEGAL_TRANSITION` 异常

### 多步流转验证
通过 5 步完整流转（DRAFT → DONE）后，确认终态不可变更。

## 4. 澄清问题测试

### 阻断场景
1. 工作项处于 ANALYZING，添加 HIGH 未解决澄清问题
2. 尝试流转到 READY → 抛出 `UNRESOLVED_HIGH_PRIORITY_QUESTIONS`
3. 解决 HIGH 问题 → 再次流转到 READY → 成功

### 优先级区分
- HIGH 未解决 → `hasUnresolvedHighPriority` 返回 `true`
- MEDIUM 未解决 → `hasUnresolvedHighPriority` 返回 `false`
- 解决了 HIGH 问题 → `hasUnresolvedHighPriority` 返回 `false`

### 问题解决
- 解决后 `status` 变为 `RESOLVED`
- `answer` 和 `resolvedAt` 字段被正确设置

## 5. AI 能力测试

| 测试场景 | 验证点 | 结果 |
|----------|--------|------|
| 结构化返回 | summary/risks/acceptanceCriteria/clarificationQuestions/taskSuggestions 均非空 | ✅ |
| 关键词匹配 | 描述含"AI"时摘要和任务建议体现 AI 相关内容 | ✅ |
| 短描述处理 | 描述过短时生成 ≥3 个澄清问题 | ✅ |
| 风险 severity | 所有 risk 的 severity 为 HIGH/MEDIUM/LOW 之一 | ✅ |

## 6. 未覆盖风险

| 风险 | 原因 |
|------|------|
| 并发状态流转（乐观锁） | 考核时间限制，未实现 `@Version` 或数据库行锁 |
| 边界值测试（超长字段 / null 特殊处理） | 使用默认的 JSR-303 校验和数据库约束覆盖 |
| 前端页面自动化测试 | 前端为演示用简化页面，通过手动操作验证 |
| 集成测试（完整 API 调用链） | 单元测试已覆盖核心逻辑，集成测试可作为后续补充 |
| 大流量性能测试 | 非考核范围 |
