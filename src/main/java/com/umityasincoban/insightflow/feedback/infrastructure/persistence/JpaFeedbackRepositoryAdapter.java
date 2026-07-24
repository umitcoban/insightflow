package com.umityasincoban.insightflow.feedback.infrastructure.persistence;

import com.umityasincoban.insightflow.customer.domain.CustomerId;
import com.umityasincoban.insightflow.feedback.domain.Feedback;
import com.umityasincoban.insightflow.feedback.domain.FeedbackId;
import com.umityasincoban.insightflow.feedback.domain.FeedbackNote;
import com.umityasincoban.insightflow.feedback.domain.FeedbackPriority;
import com.umityasincoban.insightflow.feedback.domain.FeedbackRepository;
import com.umityasincoban.insightflow.feedback.domain.FeedbackSource;
import com.umityasincoban.insightflow.feedback.domain.FeedbackStatus;
import com.umityasincoban.insightflow.tenancy.domain.TenantId;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.umityasincoban.insightflow.feedback.domain.FeedbackId;
import com.umityasincoban.insightflow.feedback.domain.FeedbackRiskLevel;
import com.umityasincoban.insightflow.feedback.domain.FeedbackSentiment;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class JpaFeedbackRepositoryAdapter implements FeedbackRepository {
	
	private final FeedbackJpaRepository feedbackJpaRepository;
	private final FeedbackNoteJpaRepository feedbackNoteJpaRepository;
	private final FeedbackPersistenceMapper feedbackPersistenceMapper;
	
	public JpaFeedbackRepositoryAdapter(
			FeedbackJpaRepository feedbackJpaRepository,
			FeedbackNoteJpaRepository feedbackNoteJpaRepository,
			FeedbackPersistenceMapper feedbackPersistenceMapper
	) {
		this.feedbackJpaRepository = feedbackJpaRepository;
		this.feedbackNoteJpaRepository = feedbackNoteJpaRepository;
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
	
	@Override
	public void applyAiAnalysis(
			TenantId tenantId,
			FeedbackId feedbackId,
			FeedbackSentiment sentiment,
			String category,
			FeedbackRiskLevel riskLevel,
			String aiSummary,
			String suggestedAction
	) {
		FeedbackEntity entity = feedbackJpaRepository.findByTenantIdAndId(
				tenantId.value(),
				feedbackId.value()
		).orElseThrow();
		
		entity.applyAiAnalysis(
				sentiment,
				category,
				riskLevel,
				aiSummary,
				suggestedAction
		);
	}
	
	@Override
	public Feedback updateStatus(TenantId tenantId, FeedbackId feedbackId, FeedbackStatus status) {
		FeedbackEntity entity = findEntity(tenantId, feedbackId);
		entity.updateStatus(status);
		return feedbackPersistenceMapper.toDomain(entity);
	}
	
	@Override
	public Feedback updatePriority(TenantId tenantId, FeedbackId feedbackId, FeedbackPriority priority) {
		FeedbackEntity entity = findEntity(tenantId, feedbackId);
		entity.updatePriority(priority);
		return feedbackPersistenceMapper.toDomain(entity);
	}
	
	@Override
	public Feedback assignTo(TenantId tenantId, FeedbackId feedbackId, String assignedTo) {
		FeedbackEntity entity = findEntity(tenantId, feedbackId);
		entity.assignTo(assignedTo);
		return feedbackPersistenceMapper.toDomain(entity);
	}
	
	@Override
	public Feedback archive(TenantId tenantId, FeedbackId feedbackId) {
		FeedbackEntity entity = findEntity(tenantId, feedbackId);
		entity.archive();
		return feedbackPersistenceMapper.toDomain(entity);
	}
	
	@Override
	public Feedback restore(TenantId tenantId, FeedbackId feedbackId) {
		FeedbackEntity entity = findEntity(tenantId, feedbackId);
		entity.restore();
		return feedbackPersistenceMapper.toDomain(entity);
	}
	
	@Override
	public FeedbackNote addNote(TenantId tenantId, FeedbackId feedbackId, String author, String content) {
		FeedbackNoteEntity saved = feedbackNoteJpaRepository.save(new FeedbackNoteEntity(
				tenantId.value(),
				feedbackId.value(),
				author,
				content
		));
		return toNote(saved);
	}
	
	@Override
	public List<FeedbackNote> listNotes(TenantId tenantId, FeedbackId feedbackId) {
		return feedbackNoteJpaRepository.findByTenantIdAndFeedbackIdOrderByCreatedAtAsc(tenantId.value(), feedbackId.value())
				.stream()
				.map(JpaFeedbackRepositoryAdapter::toNote)
				.toList();
	}
	
	private FeedbackEntity findEntity(TenantId tenantId, FeedbackId feedbackId) {
		return feedbackJpaRepository.findByTenantIdAndId(tenantId.value(), feedbackId.value()).orElseThrow();
	}
	
	private static FeedbackNote toNote(FeedbackNoteEntity entity) {
		return new FeedbackNote(
				entity.getId(),
				TenantId.of(entity.getTenantId()),
				FeedbackId.of(entity.getFeedbackId()),
				entity.getAuthor(),
				entity.getContent(),
				entity.getCreatedAt()
		);
	}
	
}
