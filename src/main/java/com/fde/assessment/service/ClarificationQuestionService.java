package com.fde.assessment.service;

import com.fde.assessment.exception.BusinessException;
import com.fde.assessment.model.dto.QuestionCreateRequest;
import com.fde.assessment.model.dto.QuestionResolveRequest;
import com.fde.assessment.model.dto.QuestionResponse;
import com.fde.assessment.model.entity.ClarificationQuestion;
import com.fde.assessment.repository.ClarificationQuestionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ClarificationQuestionService {

    private final ClarificationQuestionRepository clarificationQuestionRepository;

    public ClarificationQuestionService(ClarificationQuestionRepository clarificationQuestionRepository) {
        this.clarificationQuestionRepository = clarificationQuestionRepository;
    }

    @Transactional
    public QuestionResponse addQuestion(Long workItemId, QuestionCreateRequest req) {
        ClarificationQuestion entity = new ClarificationQuestion();
        entity.setWorkItemId(workItemId);
        entity.setContent(req.getContent());
        entity.setSeverity(req.getSeverity());
        entity.setStatus("UNRESOLVED");
        entity.setCreatedAt(LocalDateTime.now());
        clarificationQuestionRepository.saveAndFlush(entity);
        return QuestionResponse.from(entity);
    }

    @Transactional
    public QuestionResponse resolveQuestion(Long questionId, QuestionResolveRequest req) {
        ClarificationQuestion entity = clarificationQuestionRepository.findById(questionId)
                .orElseThrow(() -> new BusinessException("QUESTION_NOT_FOUND", "澄清问题不存在"));
        entity.setAnswer(req.getAnswer());
        entity.setStatus("RESOLVED");
        entity.setResolvedAt(LocalDateTime.now());
        clarificationQuestionRepository.save(entity);
        return QuestionResponse.from(entity);
    }

    public List<QuestionResponse> getQuestions(Long workItemId) {
        return clarificationQuestionRepository.findByWorkItemIdOrderByCreatedAtDesc(workItemId)
                .stream()
                .map(QuestionResponse::from)
                .collect(Collectors.toList());
    }

    public boolean hasUnresolvedHighPriority(Long workItemId) {
        return clarificationQuestionRepository
                .countByWorkItemIdAndSeverityAndStatus(workItemId, "HIGH", "UNRESOLVED") > 0;
    }
}
