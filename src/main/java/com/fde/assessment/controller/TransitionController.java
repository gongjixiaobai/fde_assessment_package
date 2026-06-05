package com.fde.assessment.controller;

import com.fde.assessment.model.dto.TransitionRequest;
import com.fde.assessment.model.dto.TransitionResponse;
import com.fde.assessment.model.dto.WorkItemResponse;
import com.fde.assessment.repository.StatusTransitionRepository;
import com.fde.assessment.service.WorkItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/workitems/{workItemId}/transitions")
@Tag(name = "状态流转", description = "工作项状态变更、历史查询")
public class TransitionController {

    private final WorkItemService workItemService;
    private final StatusTransitionRepository statusTransitionRepository;

    public TransitionController(WorkItemService workItemService,
                                StatusTransitionRepository statusTransitionRepository) {
        this.workItemService = workItemService;
        this.statusTransitionRepository = statusTransitionRepository;
    }

    @PostMapping
    @Operation(summary = "执行状态流转", description = "将工作项流转到目标状态，自动校验合法性并检查阻断规则")
    public WorkItemResponse transit(@Parameter(description = "工作项ID") @PathVariable Long workItemId,
                                    @Valid @RequestBody TransitionRequest request) {
        return workItemService.transit(workItemId, request);
    }

    @GetMapping
    @Operation(summary = "流转历史", description = "查询工作项的状态流转历史记录")
    public List<TransitionResponse> getTransitions(@Parameter(description = "工作项ID") @PathVariable Long workItemId) {
        return statusTransitionRepository.findByWorkItemIdOrderByCreatedAtDesc(workItemId)
                .stream()
                .map(TransitionResponse::from)
                .collect(Collectors.toList());
    }
}
