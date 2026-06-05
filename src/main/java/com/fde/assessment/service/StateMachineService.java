package com.fde.assessment.service;

import com.fde.assessment.exception.BusinessException;
import com.fde.assessment.exception.ErrorCode;
import com.fde.assessment.model.entity.StatusTransition;
import com.fde.assessment.model.entity.WorkItem;
import com.fde.assessment.repository.ClarificationQuestionRepository;
import com.fde.assessment.repository.StatusTransitionRepository;
import com.fde.assessment.repository.WorkItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

@Service
public class StateMachineService {

    private static final Map<String, Set<String>> STATES = new LinkedHashMap<>();
    private static final Set<String> BLOCKED_STATUSES = Set.of("READY", "IN_PROGRESS", "TESTING", "DONE");

    static {
        STATES.put("DRAFT", Set.of("ANALYZING"));
        STATES.put("ANALYZING", Set.of("DRAFT", "READY"));
        STATES.put("READY", Set.of("ANALYZING", "IN_PROGRESS"));
        STATES.put("IN_PROGRESS", Set.of("READY", "TESTING"));
        STATES.put("TESTING", Set.of("IN_PROGRESS", "DONE"));
        STATES.put("DONE", Collections.emptySet());
    }

    private final WorkItemRepository workItemRepository;
    private final ClarificationQuestionRepository clarificationQuestionRepository;
    private final StatusTransitionRepository statusTransitionRepository;

    public StateMachineService(WorkItemRepository workItemRepository,
                               ClarificationQuestionRepository clarificationQuestionRepository,
                               StatusTransitionRepository statusTransitionRepository) {
        this.workItemRepository = workItemRepository;
        this.clarificationQuestionRepository = clarificationQuestionRepository;
        this.statusTransitionRepository = statusTransitionRepository;
    }

    @Transactional
    public void transit(Long workItemId, String targetStatus, String operator) {
        WorkItem workItem = workItemRepository.findById(workItemId)
                .orElseThrow(() -> new BusinessException("WORKITEM_NOT_FOUND", "工作项不存在"));

        String currentStatus = workItem.getStatus();
        if (!isValidTransition(currentStatus, targetStatus)) {
            throw new BusinessException("ILLEGAL_TRANSITION",
                    "不允许从 " + currentStatus + " 流转到 " + targetStatus);
        }

        if (BLOCKED_STATUSES.contains(targetStatus)) {
            int count = clarificationQuestionRepository
                    .countByWorkItemIdAndSeverityAndStatus(workItemId, "HIGH", "UNRESOLVED");
            if (count > 0) {
                throw new BusinessException("UNRESOLVED_HIGH_PRIORITY_QUESTIONS",
                        "存在未解决的高优先级澄清问题，不能转入 " + targetStatus);
            }
        }

        StatusTransition transition = new StatusTransition();
        transition.setWorkItemId(workItemId);
        transition.setFromStatus(currentStatus);
        transition.setToStatus(targetStatus);
        transition.setOperator(operator);
        transition.setCreatedAt(LocalDateTime.now());
        statusTransitionRepository.saveAndFlush(transition);

        workItem.setStatus(targetStatus);
        workItem.setUpdatedAt(LocalDateTime.now());
        try {
            workItemRepository.saveAndFlush(workItem);
        } catch (org.springframework.orm.ObjectOptimisticLockingFailureException e) {
            throw new BusinessException(ErrorCode.CONCURRENT_MODIFICATION.getCode(),
                    ErrorCode.CONCURRENT_MODIFICATION.getDefaultMessage());
        }
    }

    public boolean isValidTransition(String currentStatus, String targetStatus) {
        Set<String> allowed = STATES.get(currentStatus);
        return allowed != null && allowed.contains(targetStatus);
    }

    public Set<String> getAllowedTransitions(String currentStatus) {
        return STATES.getOrDefault(currentStatus, Collections.emptySet());
    }
}
