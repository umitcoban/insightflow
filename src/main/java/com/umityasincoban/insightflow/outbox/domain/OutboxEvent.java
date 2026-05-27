package com.umityasincoban.insightflow.outbox.domain;

import com.umityasincoban.insightflow.tenancy.domain.TenantId;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

public record OutboxEvent(
		UUID id,
		TenantId tenantId,
		String aggregateType,
		UUID aggregateId,
		String eventType,
		int eventVersion,
		Map<String, Object> payload,
		OutboxEventStatus status,
		int retryCount,
		OffsetDateTime createdAt
) {
}