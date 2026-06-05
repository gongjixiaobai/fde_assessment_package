package com.fde.assessment.service.ai;

import com.fde.assessment.model.dto.AIAnalysisResult;
import com.fde.assessment.model.dto.AIAnalysisResult.QuestionSuggestion;
import com.fde.assessment.model.dto.AIAnalysisResult.RiskItem;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Mock AI 分析服务 — 基于关键词规则返回结构化分析结果
 * 使用 @Profile("!llm") 作为默认实现，可通过 @Profile("llm") 切换为真实 LLM
 */
@Service
@Profile("!llm")
public class MockAIAnalysisService implements AIAnalysisService {

    @Override
    public AIAnalysisResult analyze(String title, String description) {
        AIAnalysisResult result = new AIAnalysisResult();

        if (description == null) description = "";

        result.setSummary(generateSummary(title, description));
        result.setRisks(generateRisks(description));
        result.setAcceptanceCriteria(generateAcceptanceCriteria(title, description));
        result.setClarificationQuestions(generateQuestions(description));
        result.setTaskSuggestions(generateTasks(title, description));

        return result;
    }

    private String generateSummary(String title, String description) {
        return "工作项「" + title + "」旨在" + extractIntent(description)
                + "。建议从需求澄清、状态流转和验收标准三个方面进行跟踪。";
    }

    private String extractIntent(String desc) {
        if (desc.contains("状态流转") || desc.contains("流转")) {
            return "建立可追踪的状态流转机制，确保工作项在生命周期的各个阶段都有清晰的状态定义和合法流转路径";
        }
        if (desc.contains("澄清") || desc.contains("问题")) {
            return "通过系统化的澄清问题管理，在开发前识别和消除需求不确定性";
        }
        if (desc.contains("AI") || desc.contains("分析")) {
            return "引入 AI 辅助分析能力，提升需求理解和风险识别的效率";
        }
        if (desc.contains("验收") || desc.contains("标准")) {
            return "明确验收标准，建立可量化的完成判定依据";
        }
        return "实现研发工作项的管理和状态追踪能力";
    }

    private List<RiskItem> generateRisks(String description) {
        List<RiskItem> risks = new ArrayList<>();
        risks.add(new RiskItem("需求边界不清", "缺少对异常流程的明确定义", "HIGH"));

        if (description.length() < 50) {
            risks.add(new RiskItem("需求描述过简", "描述信息不足，可能存在大量隐含需求未表达", "HIGH"));
        } else {
            risks.add(new RiskItem("验收标准缺失", "未明确定义工作项完成的具体判定条件", "MEDIUM"));
        }
        if (description.contains("接口") || description.contains("API")) {
            risks.add(new RiskItem("接口定义不明确", "接口返回字段和错误码未定义", "HIGH"));
        }
        if (description.contains("权限") || description.contains("角色")) {
            risks.add(new RiskItem("权限模型不清晰", "不同角色的操作权限边界未定义", "MEDIUM"));
        }

        return risks;
    }

    private List<String> generateAcceptanceCriteria(String title, String description) {
        List<String> criteria = new ArrayList<>();

        if (title.contains("状态") || title.contains("流转") || description.contains("状态")) {
            criteria.add("所有合法状态流转均可正常执行");
            criteria.add("非法状态流转返回明确的错误提示");
        }
        if (title.contains("AI") || description.contains("AI")) {
            criteria.add("AI 分析结果以结构化 JSON 形式返回");
            criteria.add("分析结果至少包含摘要、风险点和澄清问题建议");
        }
        if (description.contains("澄清")) {
            criteria.add("高优先级未解决澄清问题可成功阻断后续状态流转");
            criteria.add("解决高优先级问题后，阻断规则自动失效");
        }

        // 通用验收标准
        if (criteria.isEmpty() || criteria.size() < 2) {
            criteria.add("核心功能在演示环境中可正常运行");
            criteria.add("主要业务流程可通过前端页面完整演示");
        }

        return criteria;
    }

    private List<QuestionSuggestion> generateQuestions(String description) {
        List<QuestionSuggestion> questions = new ArrayList<>();

        if (description.length() < 50) {
            questions.add(new QuestionSuggestion("该工作项的具体业务场景是什么？请补充详细的用户故事和使用场景。", "HIGH"));
            questions.add(new QuestionSuggestion("完成本工作项需要哪些前置条件？", "MEDIUM"));
        } else {
            questions.add(new QuestionSuggestion("是否所有的异常路径都已定义？请补充异常场景的处理方式。", "MEDIUM"));
        }

        questions.add(new QuestionSuggestion("该工作项的完成对哪些下游模块有影响？", "MEDIUM"));
        questions.add(new QuestionSuggestion("是否需要考虑性能或并发场景？", "LOW"));

        return questions;
    }

    private List<String> generateTasks(String title, String description) {
        List<String> tasks = new ArrayList<>();

        if (description.contains("状态") || description.contains("流转")) {
            tasks.add("定义状态枚举及合法流转规则");
            tasks.add("实现状态机引擎核心逻辑");
            tasks.add("添加状态流转历史记录功能");
            tasks.add("编写状态流转的单元测试");
        }
        if (description.contains("AI") || description.contains("分析")) {
            tasks.add("定义 AI 分析服务的接口契约");
            tasks.add("实现 Mock AI 分析服务用于开发调试");
            tasks.add("对接真实 LLM API 替换 Mock 实现");
        }
        if (description.contains("澄清")) {
            tasks.add("实现澄清问题的 CRUD 接口");
            tasks.add("实现高优先级未解决澄清问题的阻断逻辑");
        }

        if (tasks.isEmpty()) {
            tasks.addAll(Arrays.asList(
                "需求分析和方案设计",
                "核心功能编码实现",
                "编写单元测试",
                "前后端联调验证"
            ));
        }

        return tasks;
    }
}
