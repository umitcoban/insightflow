package com.umityasincoban.insightflow.outbox.domain;

import com.umityasincoban.insightflow.tenancy.domain.TenantId;

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
}