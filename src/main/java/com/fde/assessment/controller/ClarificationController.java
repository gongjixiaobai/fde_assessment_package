package com.fde.assessment.controller;

import com.fde.assessment.model.dto.QuestionCreateRequest;
import com.fde.assessment.model.dto.QuestionResolveRequest;
import com.fde.assessment.model.dto.QuestionResponse;
import com.fde.assessment.service.ClarificationQuestionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/workitems/{workItemId}/questions")
@Tag(name = "澄清问题", description = "工作项澄清问题的新增、回复、查询")
public class ClarificationController {

    private final ClarificationQuestionService clarificationQuestionService;

    public ClarificationController(ClarificationQuestionService clarificationQuestionService) {
        this.clarificationQuestionService = clarificationQuestionService;
    }

    @GetMapping
    @Operation(summary = "澄清问题列表", description = "查询工作项下所有澄清问题")
    public List<QuestionResponse> getQuestions(@Parameter(description = "工作项ID") @PathVariable Long workItemId) {
        return clarificationQuestionService.getQuestions(workItemId);
    }

    @PostMapping
    @Operation(summary = "新增澄清问题", description = "为工作项添加新的澄清问题，初始状态为 UNRESOLVED")
    public ResponseEntity<QuestionResponse> addQuestion(@Parameter(description = "工作项ID") @PathVariable Long workItemId,
                                                         @Valid @RequestBody QuestionCreateRequest request) {
        QuestionResponse response = clarificationQuestionService.addQuestion(workItemId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{questionId}")
    @Operation(summary = "回复/解决澄清问题", description = "对澄清问题进行回复，状态变更为 RESOLVED")
    public QuestionResponse resolveQuestion(@Parameter(description = "工作项ID") @PathVariable Long workItemId,
                                            @Parameter(description = "问题ID") @PathVariable Long questionId,
                                            @Valid @RequestBody QuestionResolveRequest request) {
        return clarificationQuestionService.resolveQuestion(questionId, request);
    }
}
