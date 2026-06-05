package com.fde.assessment.model.dto;

import com.fde.assessment.model.entity.ClarificationQuestion;

import java.time.LocalDateTime;

public class QuestionResponse {

    private Long id;
    private Long workItemId;
    private String content;
    private String severity;
    private String status;
    private String answer;
    private LocalDateTime createdAt;
    private LocalDateTime resolvedAt;

    public static QuestionResponse from(ClarificationQuestion q) {
        QuestionResponse r = new QuestionResponse();
        r.id = q.getId();
        r.workItemId = q.getWorkItemId();
        r.content = q.getContent();
        r.severity = q.getSeverity();
        r.status = q.getStatus();
        r.answer = q.getAnswer();
        r.createdAt = q.getCreatedAt();
        r.resolvedAt = q.getResolvedAt();
        return r;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getWorkItemId() { return workItemId; }
    public void setWorkItemId(Long workItemId) { this.workItemId = workItemId; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getAnswer() { return answer; }
    public void setAnswer(String answer) { this.answer = answer; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(LocalDateTime resolvedAt) { this.resolvedAt = resolvedAt; }
}
