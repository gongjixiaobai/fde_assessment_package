package com.fde.assessment.model.dto;

import jakarta.validation.constraints.NotBlank;

public class QuestionCreateRequest {

    @NotBlank(message = "问题内容不能为空")
    private String content;

    @NotBlank(message = "严重程度不能为空")
    private String severity;

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }
}
