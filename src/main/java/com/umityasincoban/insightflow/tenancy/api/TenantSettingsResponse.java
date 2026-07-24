package com.umityasincoban.insightflow.tenancy.api;

import com.umityasincoban.insightflow.tenancy.domain.TenantSettings;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

public record TenantSettingsResponse(
		UUID tenantId,
		Map<String, Object> settings,
		OffsetDateTime updatedAt
) {
	
	public static TenantSettingsResponse from(TenantSettings settings) {
		return new TenantSettingsResponse(
				settings.tenantId().value(),
				settings.settings(),
				settings.updatedAt()
		);
	}
}

