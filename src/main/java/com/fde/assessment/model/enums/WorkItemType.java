package com.fde.assessment.model.enums;

/**
 * 工作项类型
 */
public enum WorkItemType {

    STORY("需求"),
    BUG("缺陷"),
    TASK("任务");

    private final String label;

    WorkItemType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
