package com.umityasincoban.insightflow.feedback.infrastructure.persistence;

import com.umityasincoban.insightflow.feedback.domain.FeedbackPriority;
import com.umityasincoban.insightflow.feedback.domain.FeedbackStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface FeedbackJpaRepository extends JpaRepository<FeedbackEntity, UUID> {
	
	Optional<FeedbackEntity> findByTenantIdAndId(UUID tenantId, UUID id);
	
	Page<FeedbackEntity> findByTenantId(UUID tenantId, Pageable pageable);
	
	Page<FeedbackEntity> findByTenantIdAndStatus(
			UUID tenantId,
			FeedbackStatus status,
			Pageable pageable
	);
	
	Page<FeedbackEntity> findByTenantIdAndPriority(
			UUID tenantId,
			FeedbackPriority priority,
			Pageable pageable
	);
}