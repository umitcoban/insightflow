package com.umityasincoban.insightflow.feedback.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface FeedbackNoteJpaRepository extends JpaRepository<FeedbackNoteEntity, UUID> {
	
	List<FeedbackNoteEntity> findByTenantIdAndFeedbackIdOrderByCreatedAtAsc(UUID tenantId, UUID feedbackId);
}

