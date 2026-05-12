package com.umityasincoban.insightflow.feedback.domain;

import com.umityasincoban.insightflow.customer.domain.CustomerId;
import com.umityasincoban.insightflow.tenancy.domain.TenantId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Map;
import java.util.Optional;

public interface FeedbackRepository {
	
	Feedback saveNew(
			TenantId tenantId,
			CustomerId customerId,
			FeedbackSource source,
			String title,
			String content,
			FeedbackPriority priority,
			Map<String, Object> metadata
	);
	
	Optional<Feedback> findByTenantIdAndId(TenantId tenantId, FeedbackId feedbackId);
	
	Page<Feedback> findByTenantId(TenantId tenantId, Pageable pageable);
	
	Page<Feedback> findByTenantIdAndStatus(TenantId tenantId, FeedbackStatus status, Pageable pageable);
	
	Page<Feedback> findByTenantIdAndPriority(TenantId tenantId, FeedbackPriority priority, Pageable pageable);
}