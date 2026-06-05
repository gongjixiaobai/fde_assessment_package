package com.fde.assessment.model.enums;

/**
 * 工作项状态枚举
 * 流转规则: DRAFT → ANALYZING ⇄ READY ⇄ IN_PROGRESS ⇄ TESTING → DONE(终态)
 */
public enum WorkItemStatus {

    DRAFT("草稿"),
    ANALYZING("待分析"),
    READY("已准备"),
    IN_PROGRESS("开发中"),
    TESTING("测试中"),
    DONE("已完成");

    private final String label;

    WorkItemStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
