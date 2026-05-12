package com.umityasincoban.insightflow.tenancy.api;

import com.umityasincoban.insightflow.tenancy.domain.Tenant;
import com.umityasincoban.insightflow.tenancy.domain.TenantStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

public record TenantResponse(
		UUID id,
		String slug,
		String name,
		TenantStatus status,
		OffsetDateTime createdAt,
		OffsetDateTime updatedAt
) {
	
	public static TenantResponse from(Tenant tenant) {
		return new TenantResponse(
				tenant.getId().value(),
				tenant.getSlug(),
				tenant.getName(),
				tenant.getStatus(),
				tenant.getCreatedAt(),
				tenant.getUpdatedAt()
		);
	}
}