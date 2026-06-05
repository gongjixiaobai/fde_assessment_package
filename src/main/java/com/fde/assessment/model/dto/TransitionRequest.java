package com.fde.assessment.model.dto;

import jakarta.validation.constraints.NotBlank;

public class TransitionRequest {

    @NotBlank(message = "目标状态不能为空")
    private String toStatus;
    private String operator;

    public String getToStatus() { return toStatus; }
    public void setToStatus(String toStatus) { this.toStatus = toStatus; }
    public String getOperator() { return operator; }
    public void setOperator(String operator) { this.operator = operator; }
}
