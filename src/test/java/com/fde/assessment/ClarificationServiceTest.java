package com.fde.assessment;

import com.fde.assessment.model.dto.QuestionCreateRequest;
import com.fde.assessment.model.dto.QuestionResolveRequest;
import com.fde.assessment.model.dto.QuestionResponse;
import com.fde.assessment.model.entity.WorkItem;
import com.fde.assessment.repository.WorkItemRepository;
import com.fde.assessment.service.ClarificationQuestionService;
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
class ClarificationServiceTest {

    @Autowired
    private ClarificationQuestionService clarificationQuestionService;

    @Autowired
    private WorkItemRepository workItemRepository;

    private Long workItemId;

    @BeforeEach
    void setUp() {
        WorkItem wi = new WorkItem();
        wi.setTitle("Test");
        wi.setDescription("Test Desc");
        wi.setType("STORY");
        wi.setPriority("P1");
        wi.setStatus("DRAFT");
        wi.setUpdatedAt(LocalDateTime.now());
        workItemRepository.saveAndFlush(wi);
        workItemId = wi.getId();
    }

    @Test
    @DisplayName("添加 HIGH 澄清问题应成功创建")
    void shouldAddHighPriorityQuestion() {
        QuestionCreateRequest req = new QuestionCreateRequest();
        req.setContent("需求边界不清");
        req.setSeverity("HIGH");

        QuestionResponse resp = clarificationQuestionService.addQuestion(workItemId, req);

        assertNotNull(resp.getId());
        assertEquals("HIGH", resp.getSeverity());
        assertEquals("UNRESOLVED", resp.getStatus());
        assertEquals("需求边界不清", resp.getContent());
    }

    @Test
    @DisplayName("添加 HIGH 问题后 hasUnresolvedHighPriority 应返回 true")
    void shouldDetectUnresolvedHighQuestion() {
        assertFalse(clarificationQuestionService.hasUnresolvedHighPriority(workItemId));

        QuestionCreateRequest req = new QuestionCreateRequest();
        req.setContent("问题");
        req.setSeverity("HIGH");
        clarificationQuestionService.addQuestion(workItemId, req);

        assertTrue(clarificationQuestionService.hasUnresolvedHighPriority(workItemId));
    }

    @Test
    @DisplayName("解决 HIGH 问题后 hasUnresolvedHighPriority 应返回 false")
    void shouldReturnFalseAfterResolvingHighQuestion() {
        QuestionCreateRequest req = new QuestionCreateRequest();
        req.setContent("问题");
        req.setSeverity("HIGH");
        QuestionResponse resp = clarificationQuestionService.addQuestion(workItemId, req);

        QuestionResolveRequest resolveReq = new QuestionResolveRequest();
        resolveReq.setAnswer("已确认需求边界");

        clarificationQuestionService.resolveQuestion(resp.getId(), resolveReq);

        assertFalse(clarificationQuestionService.hasUnresolvedHighPriority(workItemId));
    }

    @Test
    @DisplayName("MEDIUM 问题不影响 hasUnresolvedHighPriority")
    void shouldNotAffectHighPriorityCheckWithMediumQuestion() {
        QuestionCreateRequest req = new QuestionCreateRequest();
        req.setContent("中等优先级问题");
        req.setSeverity("MEDIUM");
        clarificationQuestionService.addQuestion(workItemId, req);

        assertFalse(clarificationQuestionService.hasUnresolvedHighPriority(workItemId));
    }

    @Test
    @DisplayName("查询工作项的澄清问题列表")
    void shouldReturnQuestionsForWorkItem() {
        QuestionCreateRequest req1 = new QuestionCreateRequest();
        req1.setContent("问题1");
        req1.setSeverity("HIGH");
        clarificationQuestionService.addQuestion(workItemId, req1);

        QuestionCreateRequest req2 = new QuestionCreateRequest();
        req2.setContent("问题2");
        req2.setSeverity("LOW");
        clarificationQuestionService.addQuestion(workItemId, req2);

        List<QuestionResponse> questions = clarificationQuestionService.getQuestions(workItemId);
        assertEquals(2, questions.size());
    }

    @Test
    @DisplayName("解决后的问题应有 answer 和 resolvedAt 字段")
    void shouldHaveAnswerAfterResolution() {
        QuestionCreateRequest req = new QuestionCreateRequest();
        req.setContent("待解决问题");
        req.setSeverity("HIGH");
        QuestionResponse created = clarificationQuestionService.addQuestion(workItemId, req);

        QuestionResolveRequest resolveReq = new QuestionResolveRequest();
        resolveReq.setAnswer("已解决：确认需求边界");

        QuestionResponse resolved = clarificationQuestionService.resolveQuestion(created.getId(), resolveReq);

        assertEquals("已解决：确认需求边界", resolved.getAnswer());
        assertEquals("RESOLVED", resolved.getStatus());
        assertNotNull(resolved.getResolvedAt());
    }
}
