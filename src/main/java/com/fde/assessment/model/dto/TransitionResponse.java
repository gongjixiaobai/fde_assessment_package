package com.fde.assessment.model.dto;

import com.fde.assessment.model.entity.StatusTransition;

import java.time.LocalDateTime;

public class TransitionResponse {

    private Long id;
    private Long workItemId;
    private String fromStatus;
    private String toStatus;
    private String operator;
    private LocalDateTime createdAt;

    public static TransitionResponse from(StatusTransition t) {
        TransitionResponse r = new TransitionResponse();
        r.id = t.getId();
        r.workItemId = t.getWorkItemId();
        r.fromStatus = t.getFromStatus();
        r.toStatus = t.getToStatus();
        r.operator = t.getOperator();
        r.createdAt = t.getCreatedAt();
        return r;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getWorkItemId() { return workItemId; }
    public void setWorkItemId(Long workItemId) { this.workItemId = workItemId; }
    public String getFromStatus() { return fromStatus; }
    public void setFromStatus(String fromStatus) { this.fromStatus = fromStatus; }
    public String getToStatus() { return toStatus; }
    public void setToStatus(String toStatus) { this.toStatus = toStatus; }
    public String getOperator() { return operator; }
    public void setOperator(String operator) { this.operator = operator; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
