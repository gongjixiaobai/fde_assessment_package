package com.fde.assessment.model.dto;

import java.time.LocalDateTime;

public class WorkItemResponse {

    private Long id;
    private String title;
    private String description;
    private String type;
    private String priority;
    private String status;
    private String assignee;
    private String tags;
    private String acceptanceCriteria;
    private String riskLevel;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // --- factory from entity ---
    public static WorkItemResponse from(com.fde.assessment.model.entity.WorkItem wi) {
        WorkItemResponse r = new WorkItemResponse();
        r.id = wi.getId();
        r.title = wi.getTitle();
        r.description = wi.getDescription();
        r.type = wi.getType();
        r.priority = wi.getPriority();
        r.status = wi.getStatus();
        r.assignee = wi.getAssignee();
        r.tags = wi.getTags();
        r.acceptanceCriteria = wi.getAcceptanceCriteria();
        r.riskLevel = wi.getRiskLevel();
        r.createdAt = wi.getCreatedAt();
        r.updatedAt = wi.getUpdatedAt();
        return r;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getAssignee() { return assignee; }
    public void setAssignee(String assignee) { this.assignee = assignee; }
    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }
    public String getAcceptanceCriteria() { return acceptanceCriteria; }
    public void setAcceptanceCriteria(String acceptanceCriteria) { this.acceptanceCriteria = acceptanceCriteria; }
    public String getRiskLevel() { return riskLevel; }
    public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
