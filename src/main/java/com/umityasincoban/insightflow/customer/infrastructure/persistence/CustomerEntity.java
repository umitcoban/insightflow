package com.umityasincoban.insightflow.customer.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "customers")
public class CustomerEntity {
	
	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;
	
	@Column(name = "tenant_id", nullable = false)
	private UUID tenantId;
	
	@Column(name = "external_id", length = 120)
	private String externalId;
	
	@Column(name = "email", length = 254)
	private String email;
	
	@Column(name = "full_name", length = 180)
	private String fullName;
	
	@Column(name = "plan", length = 60)
	private String plan;
	
	@Column(name = "created_at", nullable = false)
	private OffsetDateTime createdAt;
	
	@Column(name = "updated_at", nullable = false)
	private OffsetDateTime updatedAt;
	
	protected CustomerEntity() {
	}
	
	public CustomerEntity(
			UUID tenantId,
			String externalId,
			String email,
			String fullName,
			String plan
	) {
		this.tenantId = tenantId;
		this.externalId = externalId;
		this.email = email;
		this.fullName = fullName;
		this.plan = plan;
		this.createdAt = OffsetDateTime.now();
		this.updatedAt = OffsetDateTime.now();
	}
	
	public UUID getId() {
		return id;
	}
	
	public UUID getTenantId() {
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
}