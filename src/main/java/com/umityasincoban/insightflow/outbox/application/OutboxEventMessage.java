package com.umityasincoban.insightflow.outbox.application;

import java.util.Map;

public record OutboxEventMessage(
		String eventId,
		String tenantId,
		String aggregateType,
		String aggregateId,
		String eventType,
		int eventVersion,
		Map<String, Object> payload,
		String createdAt
) {
}