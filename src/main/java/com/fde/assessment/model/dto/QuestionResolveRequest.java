package com.fde.assessment.model.dto;

import jakarta.validation.constraints.NotBlank;

public class QuestionResolveRequest {

    @NotBlank(message = "回答不能为空")
    private String answer;

    public String getAnswer() { return answer; }
    public void setAnswer(String answer) { this.answer = answer; }
}
