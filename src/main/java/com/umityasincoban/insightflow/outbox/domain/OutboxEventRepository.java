package com.umityasincoban.insightflow.outbox.domain;

import com.umityasincoban.insightflow.tenancy.domain.TenantId;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface OutboxEventRepository {
	
	void savePendingEvent(
			TenantId tenantId,
			String aggregateType,
			UUID aggregateId,
			String eventType,
			int eventVersion,
			Map<String, Object> payload
	);
	
	List<OutboxEvent> findPendingEvents(int limit);
	
	void markAsPublished(UUID eventId);
	
	void markForRetry(UUID eventId, String errorMessage, OffsetDateTime nextRetryAt);
	
	void markAsFailed(UUID eventId, String errorMessage);
}