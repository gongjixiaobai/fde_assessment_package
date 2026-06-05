package com.fde.assessment.model.enums;

/**
 * 澄清问题解决状态
 */
public enum QuestionStatus {

    UNRESOLVED("未解决"),
    RESOLVED("已解决");

    private final String label;

    QuestionStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
