package com.fde.assessment.controller;

import com.fde.assessment.model.dto.WorkItemCreateRequest;
import com.fde.assessment.model.dto.WorkItemDetailResponse;
import com.fde.assessment.model.dto.WorkItemResponse;
import com.fde.assessment.model.dto.WorkItemUpdateRequest;
import com.fde.assessment.service.WorkItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/workitems")
@Tag(name = "工作项管理", description = "工作项的创建、查询、修改、删除")
public class WorkItemController {

    private final WorkItemService workItemService;

    public WorkItemController(WorkItemService workItemService) {
        this.workItemService = workItemService;
    }

    @PostMapping
    @Operation(summary = "创建工作项", description = "创建新的工作项，初始状态为 DRAFT")
    public ResponseEntity<WorkItemResponse> create(@Valid @RequestBody WorkItemCreateRequest request) {
        WorkItemResponse response = workItemService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "工作项列表", description = "查询工作项列表，支持按状态、类型、优先级筛选和关键词搜索")
    public List<WorkItemResponse> findAll(
            @Parameter(description = "状态筛选") @RequestParam(required = false) String status,
            @Parameter(description = "类型筛选") @RequestParam(required = false) String type,
            @Parameter(description = "优先级筛选") @RequestParam(required = false) String priority,
            @Parameter(description = "关键词搜索") @RequestParam(required = false) String keyword,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "20") int size) {
        return workItemService.findAll(status, type, priority, keyword, page, size);
    }

    @GetMapping("/{id}")
    @Operation(summary = "工作项详情", description = "查询工作项详情，包含关联的澄清问题和流转历史")
    public WorkItemDetailResponse findById(@Parameter(description = "工作项ID") @PathVariable Long id) {
        return workItemService.findById(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新工作项", description = "更新工作项基本信息（不含状态），支持部分字段更新")
    public WorkItemResponse update(@Parameter(description = "工作项ID") @PathVariable Long id,
                                   @RequestBody WorkItemUpdateRequest request) {
        return workItemService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除工作项", description = "删除工作项及其关联的澄清问题和流转历史")
    public ResponseEntity<Void> delete(@Parameter(description = "工作项ID") @PathVariable Long id) {
        workItemService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
