package com.fde.assessment.service;

import com.fde.assessment.exception.BusinessException;
import com.fde.assessment.exception.ErrorCode;
import com.fde.assessment.model.dto.*;
import com.fde.assessment.model.entity.ClarificationQuestion;
import com.fde.assessment.model.entity.StatusTransition;
import com.fde.assessment.model.entity.WorkItem;
import com.fde.assessment.repository.ClarificationQuestionRepository;
import com.fde.assessment.repository.StatusTransitionRepository;
import com.fde.assessment.repository.WorkItemRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class WorkItemService {

    private final WorkItemRepository workItemRepository;
    private final ClarificationQuestionRepository clarificationQuestionRepository;
    private final StatusTransitionRepository statusTransitionRepository;
    private final StateMachineService stateMachineService;

    public WorkItemService(WorkItemRepository workItemRepository,
                           ClarificationQuestionRepository clarificationQuestionRepository,
                           StatusTransitionRepository statusTransitionRepository,
                           StateMachineService stateMachineService) {
        this.workItemRepository = workItemRepository;
        this.clarificationQuestionRepository = clarificationQuestionRepository;
        this.statusTransitionRepository = statusTransitionRepository;
        this.stateMachineService = stateMachineService;
    }

    @Transactional
    public WorkItemResponse create(WorkItemCreateRequest req) {
        WorkItem entity = new WorkItem();
        entity.setTitle(req.getTitle());
        entity.setDescription(req.getDescription());
        entity.setType(req.getType());
        entity.setPriority(req.getPriority());
        entity.setStatus("DRAFT");
        entity.setAssignee(req.getAssignee());
        entity.setTags(req.getTags());
        entity.setAcceptanceCriteria(req.getAcceptanceCriteria());
        entity.setRiskLevel(req.getRiskLevel());
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());
        workItemRepository.saveAndFlush(entity);
        return WorkItemResponse.from(entity);
    }

    public WorkItemDetailResponse findById(Long id) {
        WorkItem entity = workItemRepository.findById(id)
                .orElseThrow(() -> new BusinessException("WORKITEM_NOT_FOUND", "工作项不存在"));
        WorkItemDetailResponse detail = new WorkItemDetailResponse();
        detail.setId(entity.getId());
        detail.setTitle(entity.getTitle());
        detail.setDescription(entity.getDescription());
        detail.setType(entity.getType());
        detail.setPriority(entity.getPriority());
        detail.setStatus(entity.getStatus());
        detail.setAssignee(entity.getAssignee());
        detail.setTags(entity.getTags());
        detail.setAcceptanceCriteria(entity.getAcceptanceCriteria());
        detail.setRiskLevel(entity.getRiskLevel());
        detail.setCreatedAt(entity.getCreatedAt());
        detail.setUpdatedAt(entity.getUpdatedAt());

        List<ClarificationQuestion> questions = clarificationQuestionRepository.findByWorkItemIdOrderByCreatedAtDesc(id);
        detail.setQuestions(questions.stream().map(QuestionResponse::from).collect(Collectors.toList()));

        List<StatusTransition> transitions = statusTransitionRepository.findByWorkItemIdOrderByCreatedAtDesc(id);
        detail.setTransitions(transitions.stream().map(TransitionResponse::from).collect(Collectors.toList()));

        return detail;
    }

    public List<WorkItemResponse> findAll(String status, String type, String priority, String keyword, int page, int size) {
        Specification<WorkItem> spec = Specification.where(null);

        if (status != null && !status.isEmpty()) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), status));
        }
        if (type != null && !type.isEmpty()) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("type"), type));
        }
        if (priority != null && !priority.isEmpty()) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("priority"), priority));
        }
        if (keyword != null && !keyword.isEmpty()) {
            String pattern = "%" + keyword + "%";
            spec = spec.and((root, query, cb) ->
                    cb.or(
                            cb.like(root.get("title"), pattern),
                            cb.like(root.get("description"), pattern)
                    ));
        }

        PageRequest pageRequest = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "updatedAt"));
        List<WorkItem> entities = workItemRepository.findAll(spec, pageRequest).getContent();
        return entities.stream().map(WorkItemResponse::from).collect(Collectors.toList());
    }

    @Transactional
    public WorkItemResponse update(Long id, WorkItemUpdateRequest req) {
        WorkItem entity = workItemRepository.findById(id)
                .orElseThrow(() -> new BusinessException("WORKITEM_NOT_FOUND", "工作项不存在"));
        if (req.getTitle() != null) entity.setTitle(req.getTitle());
        if (req.getDescription() != null) entity.setDescription(req.getDescription());
        if (req.getType() != null) entity.setType(req.getType());
        if (req.getPriority() != null) entity.setPriority(req.getPriority());
        if (req.getAssignee() != null) entity.setAssignee(req.getAssignee());
        if (req.getTags() != null) entity.setTags(req.getTags());
        if (req.getAcceptanceCriteria() != null) entity.setAcceptanceCriteria(req.getAcceptanceCriteria());
        if (req.getRiskLevel() != null) entity.setRiskLevel(req.getRiskLevel());
        entity.setUpdatedAt(LocalDateTime.now());

        try {
            workItemRepository.saveAndFlush(entity);
        } catch (org.springframework.orm.ObjectOptimisticLockingFailureException e) {
            throw new BusinessException(ErrorCode.CONCURRENT_MODIFICATION.getCode(),
                    ErrorCode.CONCURRENT_MODIFICATION.getDefaultMessage());
        }
        return WorkItemResponse.from(entity);
    }

    @Transactional
    public void delete(Long id) {
        workItemRepository.deleteById(id);
    }

    public WorkItemResponse transit(Long id, TransitionRequest req) {
        stateMachineService.transit(id, req.getToStatus(), req.getOperator());
        WorkItem entity = workItemRepository.findById(id)
                .orElseThrow(() -> new BusinessException("WORKITEM_NOT_FOUND", "工作项不存在"));
        return WorkItemResponse.from(entity);
    }
}
