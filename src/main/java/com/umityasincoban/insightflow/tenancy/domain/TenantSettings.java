package com.umityasincoban.insightflow.tenancy.domain;

import java.time.OffsetDateTime;
import java.util.Map;

public record TenantSettings(
		TenantId tenantId,
		Map<String, Object> settings,
		OffsetDateTime updatedAt
) {
}

