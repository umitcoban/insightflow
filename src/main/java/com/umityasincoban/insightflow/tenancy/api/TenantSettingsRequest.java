package com.umityasincoban.insightflow.tenancy.api;

import jakarta.validation.constraints.NotNull;

import java.util.Map;

public record TenantSettingsRequest(
		@NotNull(message = "Tenant settings are required")
		Map<String, Object> settings
) {
}

