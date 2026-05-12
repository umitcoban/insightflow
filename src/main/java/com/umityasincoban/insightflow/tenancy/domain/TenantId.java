package com.umityasincoban.insightflow.tenancy.domain;

import java.util.Objects;
import java.util.UUID;

public record TenantId(UUID value) {
	public TenantId {
		Objects.requireNonNull(value, "Tenant id cannot be null");
	}
	public static TenantId of(UUID value) {
		return new TenantId(value);
	}
}
