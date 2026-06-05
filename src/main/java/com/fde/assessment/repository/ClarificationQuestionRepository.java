package com.fde.assessment.repository;

import com.fde.assessment.model.entity.ClarificationQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClarificationQuestionRepository extends JpaRepository<ClarificationQuestion, Long> {

    List<ClarificationQuestion> findByWorkItemIdOrderByCreatedAtDesc(Long workItemId);

    int countByWorkItemIdAndSeverityAndStatus(Long workItemId, String severity, String status);

    int deleteByWorkItemId(Long workItemId);
}
