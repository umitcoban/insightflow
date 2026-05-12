package com.umityasincoban.insightflow.feedback.infrastructure.persistence;

import com.umityasincoban.insightflow.customer.domain.CustomerId;
import com.umityasincoban.insightflow.feedback.domain.Feedback;
import com.umityasincoban.insightflow.feedback.domain.FeedbackId;
import com.umityasincoban.insightflow.feedback.domain.FeedbackPriority;
import com.umityasincoban.insightflow.feedback.domain.FeedbackRepository;
import com.umityasincoban.insightflow.feedback.domain.FeedbackSource;
import com.umityasincoban.insightflow.feedback.domain.FeedbackStatus;
import com.umityasincoban.insightflow.tenancy.domain.TenantId;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class JpaFeedbackRepositoryAdapter implements FeedbackRepository {
	
	private final FeedbackJpaRepository feedbackJpaRepository;
	private final FeedbackPersistenceMapper feedbackPersistenceMapper;
	
	public JpaFeedbackRepositoryAdapter(
			FeedbackJpaRepository feedbackJpaRepository,
			FeedbackPersistenceMapper feedbackPersistenceMapper
	) {
		this.feedbackJpaRepository = feedbackJpaRepository;
		this.feedbackPersistenceMapper = feedbackPersistenceMapper;
	}
	
	@Override
	public Feedback saveNew(
			TenantId tenantId,
			CustomerId customerId,
			FeedbackSource source,
			String title,
			String content,
			FeedbackPriority priority,
			Map<String, Object> metadata
	) {
		FeedbackEntity entity = new FeedbackEntity(
				tenantId.value(),
				customerId == null ? null : customerId.value(),
				source,
				title,
				content,
				FeedbackStatus.NEW,
				priority,
				metadata
		);
		
		FeedbackEntity savedEntity = feedbackJpaRepository.save(entity);
		
		return feedbackPersistenceMapper.toDomain(savedEntity);
	}
	
	@Override
	public Optional<Feedback> findByTenantIdAndId(TenantId tenantId, FeedbackId feedbackId) {
		return feedbackJpaRepository.findByTenantIdAndId(tenantId.value(), feedbackId.value())
				.map(feedbackPersistenceMapper::toDomain);
	}
	
	@Override
	public Page<Feedback> findByTenantId(TenantId tenantId, Pageable pageable) {
		return feedbackJpaRepository.findByTenantId(tenantId.value(), pageable)
				.map(feedbackPersistenceMapper::toDomain);
	}
	
	@Override
	public Page<Feedback> findByTenantIdAndStatus(
			TenantId tenantId,
			FeedbackStatus status,
			Pageable pageable
	) {
		return feedbackJpaRepository.findByTenantIdAndStatus(tenantId.value(), status, pageable)
				.map(feedbackPersistenceMapper::toDomain);
	}
	
	@Override
	public Page<Feedback> findByTenantIdAndPriority(
			TenantId tenantId,
			FeedbackPriority priority,
			Pageable pageable
	) {
		return feedbackJpaRepository.findByTenantIdAndPriority(tenantId.value(), priority, pageable)
				.map(feedbackPersistenceMapper::toDomain);
	}
}