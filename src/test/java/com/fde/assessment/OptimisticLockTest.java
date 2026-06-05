package com.fde.assessment;

import com.fde.assessment.exception.BusinessException;
import com.fde.assessment.model.dto.WorkItemResponse;
import com.fde.assessment.model.dto.WorkItemUpdateRequest;
import com.fde.assessment.model.entity.WorkItem;
import com.fde.assessment.repository.WorkItemRepository;
import com.fde.assessment.service.StateMachineService;
import com.fde.assessment.service.WorkItemService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 乐观锁测试 — 加分项 3
 */
@SpringBootTest
class OptimisticLockTest {

    @Autowired
    private WorkItemRepository workItemRepository;

    @Autowired
    private WorkItemService workItemService;

    @Autowired
    private StateMachineService stateMachineService;

    @Autowired
    private TransactionTemplate transactionTemplate;

    private Long workItemId;
    private Long originalVersion;

    @BeforeEach
    void setUp() {
        transactionTemplate.execute(status -> {
            WorkItem wi = new WorkItem();
            wi.setTitle("Optimistic Lock Test");
            wi.setDescription("Testing concurrent modification protection");
            wi.setType("STORY");
            wi.setPriority("P1");
            wi.setStatus("DRAFT");
            wi.setCreatedAt(LocalDateTime.now());
            wi.setUpdatedAt(LocalDateTime.now());
            workItemRepository.saveAndFlush(wi);
            workItemId = wi.getId();
            originalVersion = wi.getVersion();
            return null;
        });
    }

    @Test
    @DisplayName("正常更新应成功并递增版本号")
    void shouldIncrementVersionOnUpdate() {
        WorkItemUpdateRequest req = new WorkItemUpdateRequest();
        req.setTitle("Updated Title");
        workItemService.update(workItemId, req);

        WorkItem updated = workItemRepository.findById(workItemId).orElseThrow();
        assertEquals("Updated Title", updated.getTitle());
        assertTrue(updated.getVersion() > originalVersion,
                "Version should increment after update, expected >" + originalVersion + " but got " + updated.getVersion());
    }

    @Test
    @DisplayName("并发修改应抛出 ObjectOptimisticLockingFailureException")
    void shouldDetectConcurrentModification() {
        // TX1: load and update successfully
        transactionTemplate.execute(status -> {
            WorkItem e = workItemRepository.findById(workItemId).orElseThrow();
            e.setTitle("User 1 Update");
            e.setUpdatedAt(LocalDateTime.now());
            workItemRepository.saveAndFlush(e);
            return null;
        });

        // TX2: try to save a new entity with stale version — 
        // ObjectOptimisticLockingFailureException propagates out of transactionTemplate
        assertThrows(ObjectOptimisticLockingFailureException.class, () -> {
            transactionTemplate.execute(status -> {
                WorkItem stale = new WorkItem();
                stale.setId(workItemId);
                stale.setTitle("User 2 Update");
                stale.setType("STORY");
                stale.setPriority("P1");
                stale.setStatus("DRAFT");
                stale.setVersion(originalVersion); // stale version
                stale.setCreatedAt(LocalDateTime.now());
                stale.setUpdatedAt(LocalDateTime.now());
                workItemRepository.saveAndFlush(stale);
                return null;
            });
        });
    }

    @Test
    @DisplayName("顺序更新应成功（Service 加载最新版本）")
    void shouldHandleSequentialUpdates() {
        transactionTemplate.execute(status -> {
            WorkItemUpdateRequest req = new WorkItemUpdateRequest();
            req.setTitle("First");
            workItemService.update(workItemId, req);
            return null;
        });

        transactionTemplate.execute(status -> {
            WorkItemUpdateRequest req = new WorkItemUpdateRequest();
            req.setTitle("Second");
            WorkItemResponse resp = workItemService.update(workItemId, req);
            assertEquals("Second", resp.getTitle());
            return null;
        });
    }

    @Test
    @DisplayName("状态流转应正确读取当前版本")
    void shouldProtectStateTransitionFromConcurrentModification() {
        transactionTemplate.execute(status -> {
            stateMachineService.transit(workItemId, "ANALYZING", "user1");
            return null;
        });

        transactionTemplate.execute(status -> {
            stateMachineService.transit(workItemId, "READY", "user2");
            WorkItem result = workItemRepository.findById(workItemId).orElseThrow();
            assertEquals("READY", result.getStatus());
            return null;
        });
    }
}
