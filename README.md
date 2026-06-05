# FDE / AI Coding 实操考题包

本考题包用于评估候选人在 AI Native 团队和 FDE（Feature Delivery Engineer）模式下的适应能力。考核周期建议为 **2 天**。

## 包结构

```text
fde_assessment_package/
├── 01_通用业务场景.md
├── 02_提交规范.md
├── candidate-frontend/
│   └── 前端方向_候选人题目.md
├── candidate-backend/
│   └── 后端方向_候选人题目.md
├── templates/
│   ├── process.md
│   ├── ai-usage.md
│   ├── api-design-proposal.md
│   └── test-plan.md
├── sample-data/
│   └── work-items.seed.json
└── interviewer-only/
    ├── 评分表_面试官用.md
    └── 复盘面试问题.md
```

## 发放建议

- 发给前端候选人：`01_通用业务场景.md`、`02_提交规范.md`、`candidate-frontend/前端方向_候选人题目.md`、`templates/`、`sample-data/`。
- 发给后端候选人：`01_通用业务场景.md`、`02_提交规范.md`、`candidate-backend/后端方向_候选人题目.md`、`templates/`、`sample-data/`。
- `interviewer-only/` 仅供 HR、面试官和评审人员使用，不建议发给候选人。

## 设计原则

本考题不提供详细 API 端点定义、不提供完整数据表结构、不提供完整 UI 设计稿，目的是考察候选人的需求理解、接口设计、工程拆解、AI Coding 使用、测试验证和端到端思考能力。
