package com.fde.assessment.model.dto;

import java.util.List;

/**
 * AI 分析结果 — 结构化返回
 */
public class AIAnalysisResult {

    private String summary;
    private List<RiskItem> risks;
    private List<String> acceptanceCriteria;
    private List<QuestionSuggestion> clarificationQuestions;
    private List<String> taskSuggestions;

    public static class RiskItem {
        private String type;
        private String description;
        private String severity;

        public RiskItem() {}
        public RiskItem(String type, String description, String severity) {
            this.type = type;
            this.description = description;
            this.severity = severity;
        }
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getSeverity() { return severity; }
        public void setSeverity(String severity) { this.severity = severity; }
    }

    public static class QuestionSuggestion {
        private String question;
        private String severity;

        public QuestionSuggestion() {}
        public QuestionSuggestion(String question, String severity) {
            this.question = question;
            this.severity = severity;
        }
        public String getQuestion() { return question; }
        public void setQuestion(String question) { this.question = question; }
        public String getSeverity() { return severity; }
        public void setSeverity(String severity) { this.severity = severity; }
    }

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }
    public List<RiskItem> getRisks() { return risks; }
    public void setRisks(List<RiskItem> risks) { this.risks = risks; }
    public List<String> getAcceptanceCriteria() { return acceptanceCriteria; }
    public void setAcceptanceCriteria(List<String> acceptanceCriteria) { this.acceptanceCriteria = acceptanceCriteria; }
    public List<QuestionSuggestion> getClarificationQuestions() { return clarificationQuestions; }
    public void setClarificationQuestions(List<QuestionSuggestion> clarificationQuestions) { this.clarificationQuestions = clarificationQuestions; }
    public List<String> getTaskSuggestions() { return taskSuggestions; }
    public void setTaskSuggestions(List<String> taskSuggestions) { this.taskSuggestions = taskSuggestions; }
}
