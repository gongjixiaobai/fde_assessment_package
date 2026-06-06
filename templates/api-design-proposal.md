# API 设计说明

## 1. API 设计目标

支持工作项管理、状态流转、澄清问题维护和 AI 辅助分析等业务能力，同时为简单前端页面（Thymeleaf）提供数据接口。

设计风格：RESTful，JSON 请求/响应。

## 2. 资源或模块划分

| 模块 | 路径前缀 | 说明 |
|------|----------|------|
| 工作项管理 | `/api/workitems` | 工作项的创建、查询、修改、删除 |
| 状态流转 | `/api/workitems/{id}/transitions` | 工作项状态变更及合法性校验 |
| 澄清问题 | `/api/workitems/{id}/questions` | 工作项关联的澄清问题新增、回复、查询 |
| AI 分析 | `/api/workitems/{id}/ai-analysis` | 触发 AI 辅助分析并获取结构化结果 |

## 3. API 列表

### 3.1 工作项管理

| 方法 | 路径 | 说明 | 请求体 | 响应 |
|------|------|------|--------|------|
| POST | `/api/workitems` | 创建工作项 | WorkItemCreateRequest | 201 WorkItemResponse |
| GET | `/api/workitems` | 工作项列表（支持筛选分页） | Query: status, type, priority, keyword, page, size | 200 List\<WorkItemResponse\> |
| GET | `/api/workitems/{id}` | 工作项详情（含关联数据） | - | 200 WorkItemDetailResponse |
| PUT | `/api/workitems/{id}` | 更新工作项 | WorkItemUpdateRequest | 200 WorkItemResponse |
| DELETE | `/api/workitems/{id}` | 删除工作项（级联删除关联） | - | 204 No Content |

**WorkItemResponse**:
```json
{
  "id": 1,
  "title": "支持工作项状态流转",
  "description": "...",
  "type": "STORY",
  "priority": "P1",
  "status": "DRAFT",
  "assignee": "candidate",
  "tags": "[\"workflow\"]",
  "acceptanceCriteria": "[\"标准1\"]",
  "riskLevel": "MEDIUM",
  "createdAt": "2026-06-05T12:00:00",
  "updatedAt": "2026-06-05T12:00:00"
}
```

**WorkItemDetailResponse** (继承 WorkItemResponse):
```json
{
  "...以上全部字段...",
  "questions": [{ QuestionResponse }],
  "transitions": [{ TransitionResponse }]
}
```

### 3.2 状态流转

| 方法 | 路径 | 说明 | 请求体 | 响应 |
|------|------|------|--------|------|
| POST | `/api/workitems/{id}/transitions` | 执行状态流转（含校验和阻断） | TransitionRequest | 200 WorkItemResponse |
| GET | `/api/workitems/{id}/transitions` | 流转历史记录 | - | 200 List\<TransitionResponse\> |

**TransitionRequest**:
```json
{ "toStatus": "READY", "operator": "user" }
```

**TransitionResponse**:
```json
{ "id": 1, "workItemId": 1, "fromStatus": "DRAFT", "toStatus": "ANALYZING", "operator": "user", "createdAt": "2026-06-05T12:00:00" }
```

### 3.3 澄清问题

| 方法 | 路径 | 说明 | 请求体 | 响应 |
|------|------|------|--------|------|
| GET | `/api/workitems/{id}/questions` | 澄清问题列表 | - | 200 List\<QuestionResponse\> |
| POST | `/api/workitems/{id}/questions` | 新增澄清问题 | QuestionCreateRequest | 201 QuestionResponse |
| PUT | `/api/workitems/{id}/questions/{qid}` | 回复/解决澄清问题 | QuestionResolveRequest | 200 QuestionResponse |

**QuestionCreateRequest**: `{ "content": "需求边界不清", "severity": "HIGH" }`

**QuestionResolveRequest**: `{ "answer": "已确认需求边界" }`

**QuestionResponse**:
```json
{ "id": 1, "workItemId": 1, "content": "需求边界不清", "severity": "HIGH", "status": "RESOLVED", "answer": "已确认", "createdAt": "...", "resolvedAt": "..." }
```

### 3.4 AI 分析

| 方法 | 路径 | 说明 | 请求体 | 响应 |
|------|------|------|--------|------|
| POST | `/api/workitems/{id}/ai-analysis` | 触发 AI 分析 | - | 200 AIAnalysisResult |
| GET | `/api/workitems/{id}/ai-analysis/allowed` | 获取当前状态允许流转到的状态 | - | 200 { currentStatus, allowedTransitions } |

## 4. 状态流转错误设计

### 错误码体系

| 错误码 | HTTP 状态 | 说明 |
|--------|-----------|------|
| `WORKITEM_NOT_FOUND` | 400 | 工作项不存在 |
| `ILLEGAL_TRANSITION` | 400 | 非法状态流转（含错误消息说明原因） |
| `UNRESOLVED_HIGH_PRIORITY_QUESTIONS` | 400 | 存在未解决的高优先级澄清问题，不能进入后续状态 |
| `VALIDATION_ERROR` | 400 | 请求参数校验失败（details 含字段级别错误） |
| `QUESTION_NOT_FOUND` | 400 | 澄清问题不存在 |
| `INTERNAL_ERROR` | 500 | 服务器内部错误 |

### 统一错误响应格式

```json
{
  "code": "ILLEGAL_TRANSITION",
  "message": "不允许从 DRAFT 流转到 READY",
  "timestamp": "2026-06-05T12:00:00",
  "details": {}
}
```

### 状态流转规则

| 当前状态 | 允许流转到 | 阻断条件 |
|----------|-----------|----------|
| DRAFT | ANALYZING | - |
| ANALYZING | DRAFT, READY | → READY 时检查未解决 HIGH 澄清问题 |
| READY | ANALYZING, IN_PROGRESS | → IN_PROGRESS 时检查未解决 HIGH 澄清问题 |
| IN_PROGRESS | READY, TESTING | → TESTING 时检查未解决 HIGH 澄清问题 |
| TESTING | IN_PROGRESS, DONE | → DONE 时检查未解决 HIGH 澄清问题 |
| DONE | (不允许流转) | - |

## 5. AI 分析结果设计

```json
{
  "summary": "生成的需求摘要文本",
  "risks": [
    {
      "type": "需求边界不清",
      "description": "缺少对异常流程的定义",
      "severity": "HIGH"
    }
  ],
  "acceptanceCriteria": [
    "所有合法状态流转均可正常执行",
    "非法状态流转返回明确的错误提示"
  ],
  "clarificationQuestions": [
    {
      "question": "该工作项的具体业务场景是什么？",
      "severity": "HIGH"
    }
  ],
  "taskSuggestions": [
    "定义状态枚举及合法流转规则",
    "实现状态机引擎核心逻辑"
  ]
}
```

### Mock 策略说明

当前使用 `MockAIAnalysisService` (`@Profile("!llm")`)，基于工作项描述中的关键词（状态/流转/AI/澄清/验收）匹配生成不同的分析结果。切换真实 LLM 只需：
1. 新建 `LLMAIAnalysisService` 实现 `AIAnalysisService` 接口
2. 添加 `@Profile("llm")` 注解
3. 在 `application.yml` 中设置 `spring.profiles.active: llm`

## 6. 前后端协作说明

前端为 Thymeleaf 模板页面，通过原生 JavaScript `fetch` API 调用后端 REST 接口：

| 页面 | 路径 | 调用的 API |
|------|------|-----------|
| 工作项列表 | `/` | GET /api/workitems, DELETE /api/workitems/{id} |
| 工作项详情 | `/detail?id={id}` | GET /api/workitems/{id}, POST /api/workitems/{id}/transitions, GET/POST/PUT questions, POST ai-analysis |

前端通过 `apiGet`/`apiPost`/`apiPut`/`apiDelete` 工具函数统一处理请求和错误。

## 7. 后续扩展

- 分页 Token 机制（当前为 offset/limit）
- 批量操作 API（批量更新状态）
- 工作项关联关系 API
- 用户认证和权限控制（JWT + RBAC）
- OpenAPI/Swagger 文档生成
