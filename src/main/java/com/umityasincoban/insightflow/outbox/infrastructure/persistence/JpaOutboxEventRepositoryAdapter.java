package com.umityasincoban.insightflow.outbox.infrastructure.persistence;

import com.umityasincoban.insightflow.outbox.domain.OutboxEventRepository;
import com.umityasincoban.insightflow.tenancy.domain.TenantId;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.UUID;

@Repository
public class JpaOutboxEventRepositoryAdapter implements OutboxEventRepository {
	
	private final OutboxEventJpaRepository outboxEventJpaRepository;
	
	public JpaOutboxEventRepositoryAdapter(OutboxEventJpaRepository outboxEventJpaRepository) {
		this.outboxEventJpaRepository = outboxEventJpaRepository;
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
}