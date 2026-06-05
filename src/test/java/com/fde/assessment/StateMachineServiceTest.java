package com.fde.assessment;

import com.fde.assessment.exception.BusinessException;
import com.fde.assessment.model.entity.ClarificationQuestion;
import com.fde.assessment.model.entity.StatusTransition;
import com.fde.assessment.model.entity.WorkItem;
import com.fde.assessment.repository.ClarificationQuestionRepository;
import com.fde.assessment.repository.StatusTransitionRepository;
import com.fde.assessment.repository.WorkItemRepository;
import com.fde.assessment.service.StateMachineService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class StateMachineServiceTest {

    @Autowired
    private StateMachineService stateMachineService;

    @Autowired
    private WorkItemRepository workItemRepository;

    @Autowired
    private ClarificationQuestionRepository questionRepository;

    @Autowired
    private StatusTransitionRepository transitionRepository;

    private Long workItemId;

    @BeforeEach
    void setUp() {
        WorkItem wi = new WorkItem();
        wi.setTitle("Test Item");
        wi.setDescription("Test Description");
        wi.setType("STORY");
        wi.setPriority("P1");
        wi.setStatus("DRAFT");
        wi.setUpdatedAt(LocalDateTime.now());
        workItemRepository.saveAndFlush(wi);
        workItemId = wi.getId();
    }

    @Test
    @DisplayName("1. DRAFT -> ANALYZING 合法流转")
    void shouldAllowDraftToAnalyzing() {
        stateMachineService.transit(workItemId, "ANALYZING", "tester");

        WorkItem updated = workItemRepository.findById(workItemId).orElseThrow();
        assertEquals("ANALYZING", updated.getStatus());

        assertEquals(1, transitionRepository.findByWorkItemIdOrderByCreatedAtDesc(workItemId).size());
    }

    @Test
    @DisplayName("2. DRAFT -> READY 非法流转（跨状态）")
    void shouldRejectDraftToReady() {
        BusinessException ex = assertThrows(BusinessException.class, () ->
                stateMachineService.transit(workItemId, "READY", "tester"));

        assertEquals("ILLEGAL_TRANSITION", ex.getCode());

        WorkItem wi = workItemRepository.findById(workItemId).orElseThrow();
        assertEquals("DRAFT", wi.getStatus());
    }

    @Test
    @DisplayName("3. ANALYZING -> DRAFT 退回流转")
    void shouldAllowAnalyzingToDraft() {
        stateMachineService.transit(workItemId, "ANALYZING", "tester");

        stateMachineService.transit(workItemId, "DRAFT", "tester");

        WorkItem updated = workItemRepository.findById(workItemId).orElseThrow();
        assertEquals("DRAFT", updated.getStatus());

        assertEquals(2, transitionRepository.findByWorkItemIdOrderByCreatedAtDesc(workItemId).size());
    }

    @Test
    @DisplayName("4. 有未解决 HIGH 问题时 ANALYZING -> READY 被阻断")
    void shouldBlockTransitionWhenUnresolvedHighQuestionExists() {
        stateMachineService.transit(workItemId, "ANALYZING", "tester");

        ClarificationQuestion q = new ClarificationQuestion();
        q.setWorkItemId(workItemId);
        q.setContent("需求边界不清");
        q.setSeverity("HIGH");
        q.setStatus("UNRESOLVED");
        q.setCreatedAt(LocalDateTime.now());
        questionRepository.save(q);

        BusinessException ex = assertThrows(BusinessException.class, () ->
                stateMachineService.transit(workItemId, "READY", "tester"));

        assertEquals("UNRESOLVED_HIGH_PRIORITY_QUESTIONS", ex.getCode());

        WorkItem wi = workItemRepository.findById(workItemId).orElseThrow();
        assertEquals("ANALYZING", wi.getStatus());
    }

    @Test
    @DisplayName("5. 解决 HIGH 问题后流转成功")
    void shouldAllowTransitionAfterResolvingHighQuestion() {
        stateMachineService.transit(workItemId, "ANALYZING", "tester");

        ClarificationQuestion q = new ClarificationQuestion();
        q.setWorkItemId(workItemId);
        q.setContent("需求边界不清");
        q.setSeverity("HIGH");
        q.setStatus("UNRESOLVED");
        q.setCreatedAt(LocalDateTime.now());
        questionRepository.save(q);

        // Resolve it
        q.setAnswer("已确认边界");
        q.setStatus("RESOLVED");
        q.setResolvedAt(LocalDateTime.now());
        questionRepository.save(q);

        stateMachineService.transit(workItemId, "READY", "tester");

        WorkItem updated = workItemRepository.findById(workItemId).orElseThrow();
        assertEquals("READY", updated.getStatus());
    }

    @Test
    @DisplayName("6. DONE 状态不可再次流转")
    void shouldRejectTransitionFromDone() {
        stateMachineService.transit(workItemId, "ANALYZING", "tester");
        stateMachineService.transit(workItemId, "READY", "tester");
        stateMachineService.transit(workItemId, "IN_PROGRESS", "tester");
        stateMachineService.transit(workItemId, "TESTING", "tester");
        stateMachineService.transit(workItemId, "DONE", "tester");

        WorkItem done = workItemRepository.findById(workItemId).orElseThrow();
        assertEquals("DONE", done.getStatus());

        BusinessException ex = assertThrows(BusinessException.class, () ->
                stateMachineService.transit(workItemId, "ANALYZING", "tester"));

        assertEquals("ILLEGAL_TRANSITION", ex.getCode());
    }

    @Test
    @DisplayName("7. 工作项不存在时应抛异常")
    void shouldThrowWhenWorkItemNotFound() {
        BusinessException ex = assertThrows(BusinessException.class, () ->
                stateMachineService.transit(999L, "ANALYZING", "tester"));

        assertEquals("WORKITEM_NOT_FOUND", ex.getCode());
    }
}
