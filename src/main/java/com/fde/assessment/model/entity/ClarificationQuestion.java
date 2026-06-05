package com.fde.assessment.model.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 澄清问题实体
 */
@Entity
@Table(name = "clarification_question")
public class ClarificationQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "work_item_id", nullable = false)
    private Long workItemId;

    @Column(nullable = false, length = 1000)
    private String content;

    @Column(nullable = false, length = 10)
    private String severity;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(length = 2000)
    private String answer;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

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

    @Override
    public String toString() {
        return "ClarificationQuestion{id=" + id + ", workItemId=" + workItemId
                + ", severity=" + severity + ", status=" + status + "}";
    }
}
