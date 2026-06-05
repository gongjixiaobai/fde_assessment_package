package com.fde.assessment.model.dto;

import java.util.List;

public class WorkItemDetailResponse extends WorkItemResponse {

    private List<QuestionResponse> questions;
    private List<TransitionResponse> transitions;

    public List<QuestionResponse> getQuestions() { return questions; }
    public void setQuestions(List<QuestionResponse> questions) { this.questions = questions; }
    public List<TransitionResponse> getTransitions() { return transitions; }
    public void setTransitions(List<TransitionResponse> transitions) { this.transitions = transitions; }
}
