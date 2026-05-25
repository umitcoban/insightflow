package com.umityasincoban.insightflow.customer.domain;

import com.umityasincoban.insightflow.tenancy.domain.TenantId;

import java.time.OffsetDateTime;
import java.util.Objects;

public class Customer {
	
	private final CustomerId id;
	private final TenantId tenantId;
	private final String externalId;
	private final String email;
	private final String fullName;
	private final String plan;
	private final OffsetDateTime createdAt;
	private final OffsetDateTime updatedAt;
	
	public Customer(
			CustomerId id,
			TenantId tenantId,
			String externalId,
			String email,
			String fullName,
			String plan,
			OffsetDateTime createdAt,
			OffsetDateTime updatedAt
	) {
		this.id = Objects.requireNonNull(id, "Customer id cannot be null");
		this.tenantId = Objects.requireNonNull(tenantId, "Tenant id cannot be null");
		this.externalId = normalizeOptional(externalId);
		this.email = normalizeOptional(email);
		this.fullName = normalizeOptional(fullName);
		this.plan = normalizeOptional(plan);
		this.createdAt = Objects.requireNonNull(createdAt, "Customer createdAt cannot be null");
		this.updatedAt = Objects.requireNonNull(updatedAt, "Customer updatedAt cannot be null");
	}
	
	public CustomerId getId() {
		return id;
	}
	
	public TenantId getTenantId() {
		return tenantId;
	}
	
	public String getExternalId() {
		return externalId;
	}
	
	public String getEmail() {
		return email;
	}
	
	public String getFullName() {
		return fullName;
	}
	
	public String getPlan() {
		return plan;
	}
	
	public OffsetDateTime getCreatedAt() {
		return createdAt;
	}
	
	public OffsetDateTime getUpdatedAt() {
		return updatedAt;
	}
	
	public boolean hasEmail() {
		return email != null && !email.isBlank();
	}
	
	public boolean hasExternalId() {
		return externalId != null && !externalId.isBlank();
	}
	
	private static String normalizeOptional(String value) {
		if (value == null || value.isBlank()) {
			return null;
		}
		
		return value.strip();
	}
}