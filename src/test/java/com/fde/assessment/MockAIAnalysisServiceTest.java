package com.fde.assessment;

import com.fde.assessment.exception.BusinessException;
import com.fde.assessment.model.dto.AIAnalysisResult;
import com.fde.assessment.service.ai.AIAnalysisService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class MockAIAnalysisServiceTest {

    @Autowired
    private AIAnalysisService aiAnalysisService;

    @Test
    @DisplayName("AI 分析应返回结构化结果（含摘要、风险、验收标准、澄清问题、任务建议）")
    void shouldReturnStructuredResult() {
        AIAnalysisResult result = aiAnalysisService.analyze(
                "支持工作项从需求分析到开发完成的状态流转",
                "实现状态流转机制，确保非法流转被拦截"
        );

        // Verify all sections are present
        assertNotNull(result.getSummary(), "摘要不应为空");
        assertFalse(result.getSummary().isEmpty());

        assertNotNull(result.getRisks(), "风险列表不应为空");
        assertFalse(result.getRisks().isEmpty());

        assertNotNull(result.getAcceptanceCriteria(), "验收标准不应为空");
        assertFalse(result.getAcceptanceCriteria().isEmpty());

        assertNotNull(result.getClarificationQuestions(), "澄清问题列表不应为空");
        assertFalse(result.getClarificationQuestions().isEmpty());

        assertNotNull(result.getTaskSuggestions(), "任务建议不应为空");
        assertFalse(result.getTaskSuggestions().isEmpty());
    }

    @Test
    @DisplayName("描述过短时应包含更多澄清问题")
    void shouldGenerateMoreQuestionsForShortDescription() {
        AIAnalysisResult result = aiAnalysisService.analyze("简单功能", "简短");

        // Short description should generate at least 3 questions
        assertTrue(result.getClarificationQuestions().size() >= 3,
                "短描述应生成更多澄清问题, actual: " + result.getClarificationQuestions().size());
    }

    @Test
    @DisplayName("描述含有关键词时应匹配对应内容")
    void shouldMatchKeywordsInDescription() {
        AIAnalysisResult result = aiAnalysisService.analyze(
                "AI 辅助生成需求澄清问题",
                "使用 AI 分析工作项内容，生成澄清问题"
        );

        // Should reference AI in summary
        assertTrue(result.getSummary().contains("AI") || result.getSummary().contains("分析"),
                "摘要应体现 AI 相关内容");

        // Should have task about AI
        boolean hasAiTask = result.getTaskSuggestions().stream()
                .anyMatch(t -> t.contains("AI") || t.contains("分析"));
        assertTrue(hasAiTask, "任务建议应包含 AI 相关任务");
    }

    @Test
    @DisplayName("风险列表应包含 severity 字段")
    void shouldHaveSeverityInRisks() {
        AIAnalysisResult result = aiAnalysisService.analyze("状态流转模块", "实现状态机");

        assertNotNull(result.getRisks());
        for (AIAnalysisResult.RiskItem risk : result.getRisks()) {
            assertNotNull(risk.getType());
            assertNotNull(risk.getDescription());
            assertNotNull(risk.getSeverity());
            assertTrue(risk.getSeverity().matches("HIGH|MEDIUM|LOW"),
                    "Severity must be HIGH/MEDIUM/LOW, got: " + risk.getSeverity());
        }
    }
}
