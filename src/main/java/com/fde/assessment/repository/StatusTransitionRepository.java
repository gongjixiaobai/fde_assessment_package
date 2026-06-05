package com.fde.assessment.repository;

import com.fde.assessment.model.entity.StatusTransition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StatusTransitionRepository extends JpaRepository<StatusTransition, Long> {

    List<StatusTransition> findByWorkItemIdOrderByCreatedAtDesc(Long workItemId);
}
