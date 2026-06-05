package com.fde.assessment.model.enums;

/**
 * 澄清问题严重程度
 */
public enum QuestionSeverity {

    HIGH("高"),
    MEDIUM("中"),
    LOW("低");

    private final String label;

    QuestionSeverity(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
