package com.fde.assessment.model.enums;

/**
 * 优先级
 */
public enum Priority {

    P0("紧急"),
    P1("高"),
    P2("中"),
    P3("低");

    private final String label;

    Priority(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
