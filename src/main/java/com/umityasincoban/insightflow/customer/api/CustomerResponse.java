package com.umityasincoban.insightflow.customer.api;

import com.umityasincoban.insightflow.customer.domain.Customer;

import java.time.OffsetDateTime;
import java.util.UUID;

public record CustomerResponse(
		UUID id,
		UUID tenantId,
		String externalId,
		String email,
		String fullName,
		String plan,
		OffsetDateTime createdAt,
		OffsetDateTime updatedAt
) {
	
	public static CustomerResponse from(Customer customer) {
		return new CustomerResponse(
				customer.getId().value(),
				customer.getTenantId().value(),
				customer.getExternalId(),
				customer.getEmail(),
				customer.getFullName(),
				customer.getPlan(),
				customer.getCreatedAt(),
				customer.getUpdatedAt()
		);
	}
}