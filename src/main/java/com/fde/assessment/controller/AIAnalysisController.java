package com.fde.assessment.controller;

import com.fde.assessment.model.dto.AIAnalysisResult;
import com.fde.assessment.model.entity.WorkItem;
import com.fde.assessment.repository.WorkItemRepository;
import com.fde.assessment.service.StateMachineService;
import com.fde.assessment.service.ai.AIAnalysisService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/workitems/{workItemId}/ai-analysis")
@Tag(name = "AI 分析", description = "AI 辅助分析能力（Mock 实现）")
public class AIAnalysisController {

    private final AIAnalysisService aiAnalysisService;
    private final WorkItemRepository workItemRepository;
    private final StateMachineService stateMachineService;

    public AIAnalysisController(AIAnalysisService aiAnalysisService,
                                WorkItemRepository workItemRepository,
                                StateMachineService stateMachineService) {
        this.aiAnalysisService = aiAnalysisService;
        this.workItemRepository = workItemRepository;
        this.stateMachineService = stateMachineService;
    }

    @PostMapping
    @Operation(summary = "触发 AI 分析", description = "对工作项进行 AI 辅助分析，返回结构化结果（摘要、风险、验收标准、澄清问题、任务建议）")
    public ResponseEntity<AIAnalysisResult> analyze(@Parameter(description = "工作项ID") @PathVariable Long workItemId) {
        var workItem = workItemRepository.findById(workItemId);
        if (workItem.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        AIAnalysisResult result = aiAnalysisService.analyze(
                workItem.get().getTitle(),
                workItem.get().getDescription()
        );
        return ResponseEntity.ok(result);
    }

    @GetMapping("/allowed")
    @Operation(summary = "获取合法流转目标", description = "查询当前状态下允许流转到的目标状态列表")
    public ResponseEntity<Map<String, Object>> getAllowedTransitions(
            @Parameter(description = "工作项ID") @PathVariable Long workItemId) {
        var workItem = workItemRepository.findById(workItemId);
        if (workItem.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        var allowed = stateMachineService.getAllowedTransitions(workItem.get().getStatus());
        return ResponseEntity.ok(Map.of(
                "currentStatus", workItem.get().getStatus(),
                "allowedTransitions", allowed
        ));
    }
}
