package com.fde.assessment;

import com.fde.assessment.model.dto.WorkItemCreateRequest;
import com.fde.assessment.model.dto.WorkItemDetailResponse;
import com.fde.assessment.model.dto.WorkItemResponse;
import com.fde.assessment.model.dto.WorkItemUpdateRequest;
import com.fde.assessment.repository.WorkItemRepository;
import com.fde.assessment.service.WorkItemService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class WorkItemServiceTest {

    @Autowired
    private WorkItemService workItemService;

    @Autowired
    private WorkItemRepository workItemRepository;

    private Long createdId;

    @BeforeEach
    void setUp() {
        WorkItemCreateRequest req = new WorkItemCreateRequest();
        req.setTitle("测试工作项");
        req.setDescription("测试描述");
        req.setType("STORY");
        req.setPriority("P1");
        req.setAssignee("tester");
        req.setTags("[\"test\"]");
        req.setAcceptanceCriteria("[\"标准1\"]");
        req.setRiskLevel("MEDIUM");

        WorkItemResponse resp = workItemService.create(req);
        createdId = resp.getId();
    }

    @Test
    @DisplayName("创建工作项应返回正确的初始状态")
    void shouldCreateWithDraftStatus() {
        WorkItemCreateRequest req = new WorkItemCreateRequest();
        req.setTitle("新工作项");
        req.setDescription("新描述");
        req.setType("BUG");
        req.setPriority("P2");

        WorkItemResponse resp = workItemService.create(req);
        assertNotNull(resp.getId());
        assertEquals("DRAFT", resp.getStatus());
        assertEquals("新工作项", resp.getTitle());
    }

    @Test
    @DisplayName("根据 ID 查询工作项应返回详情含关联数据")
    void shouldFindByIdWithDetails() {
        WorkItemDetailResponse detail = workItemService.findById(createdId);

        assertEquals("测试工作项", detail.getTitle());
        assertEquals("DRAFT", detail.getStatus());
        assertNotNull(detail.getQuestions());
        assertNotNull(detail.getTransitions());
    }

    @Test
    @DisplayName("查询列表应支持筛选")
    void shouldFilterByStatus() {
        List<WorkItemResponse> items = workItemService.findAll("DRAFT", null, null, null, 1, 20);
        assertFalse(items.isEmpty());
        assertTrue(items.stream().allMatch(i -> "DRAFT".equals(i.getStatus())));
    }

    @Test
    @DisplayName("更新工作项应保留未修改的字段")
    void shouldUpdatePartialFields() {
        WorkItemUpdateRequest update = new WorkItemUpdateRequest();
        update.setTitle("更新标题");
        update.setPriority("P0");

        WorkItemResponse resp = workItemService.update(createdId, update);
        assertEquals("更新标题", resp.getTitle());
        assertEquals("P0", resp.getPriority());
        assertEquals("STORY", resp.getType()); // unchanged
        assertEquals("DRAFT", resp.getStatus()); // unchanged
    }

    @Test
    @DisplayName("删除工作项后查询应失败")
    void shouldDeleteWorkItem() {
        workItemService.delete(createdId);

        assertTrue(workItemRepository.findById(createdId).isEmpty());
    }
}
