package com.umityasincoban.insightflow.outbox.infrastructure.persistence;

import com.umityasincoban.insightflow.outbox.domain.OutboxEvent;
import com.umityasincoban.insightflow.outbox.domain.OutboxEventRepository;
import com.umityasincoban.insightflow.outbox.domain.OutboxEventStatus;
import com.umityasincoban.insightflow.tenancy.domain.TenantId;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Repository
public class JpaOutboxEventRepositoryAdapter implements OutboxEventRepository {
	
	private final OutboxEventJpaRepository outboxEventJpaRepository;
	private final OutboxEventPersistenceMapper outboxEventPersistenceMapper;
	
	public JpaOutboxEventRepositoryAdapter(
			OutboxEventJpaRepository outboxEventJpaRepository,
			OutboxEventPersistenceMapper outboxEventPersistenceMapper
	) {
		this.outboxEventJpaRepository = outboxEventJpaRepository;
		this.outboxEventPersistenceMapper = outboxEventPersistenceMapper;
	}
	
	@Override
	public void savePendingEvent(
			TenantId tenantId,
			String aggregateType,
			UUID aggregateId,
			String eventType,
			int eventVersion,
			Map<String, Object> payload
	) {
		OutboxEventEntity entity = new OutboxEventEntity(
				UUID.randomUUID(),
				tenantId.value(),
				aggregateType,
				aggregateId,
				eventType,
				eventVersion,
				payload
		);
		
		outboxEventJpaRepository.save(entity);
	}
	
	@Override
	public List<OutboxEvent> findPendingEvents(int limit) {
		return outboxEventJpaRepository.findPendingEventsForPublishing(limit)
				.stream()
				.map(outboxEventPersistenceMapper::toDomain)
				.toList();
	}
	
	@Override
	public void markAsPublished(UUID eventId) {
		OutboxEventEntity entity = outboxEventJpaRepository.findById(eventId)
				.orElseThrow();
		
		entity.markAsPublished();
	}
	
	@Override
	public void markAsFailed(UUID eventId, String errorMessage) {
		OutboxEventEntity entity = outboxEventJpaRepository.findById(eventId)
				.orElseThrow();
		
		entity.markAsFailed(errorMessage);
	}
}