package com.umityasincoban.insightflow.tenancy.infrastructure.persistence;

import com.umityasincoban.insightflow.tenancy.domain.TenantStatus;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "tenants")
public class TenantEntity {
	
	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;
	
	@Column(unique = true, length = 80, nullable = false)
	private String slug;
	
	@Column(length = 160, nullable = false)
	private String name;
	
	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 30)
	private TenantStatus status;
	
	@Column(nullable = false, updatable = false)
	@CreationTimestamp
	private OffsetDateTime createdAt;
	
	@Column(nullable = false)
	@UpdateTimestamp
	private OffsetDateTime updatedAt;
	
	protected TenantEntity() {}
	
	public TenantEntity(String slug, String name, TenantStatus status) {
		this.slug = slug;
		this.name = name;
		this.status = status;
		this.createdAt = OffsetDateTime.now();
		this.updatedAt = OffsetDateTime.now();
	}
	
	public UUID getId() {
		return id;
	}
	
	public String getSlug() {
		return slug;
	}
	
	public String getName() {
		return name;
	}
	
	public TenantStatus getStatus() {
		return status;
	}
	
	public OffsetDateTime getCreatedAt() {
		return createdAt;
	}
	
	public OffsetDateTime getUpdatedAt() {
		return updatedAt;
	}
	
	public boolean isActive() {
		return TenantStatus.ACTIVE.equals(status);
	}
	
	public void suspend() {
		this.status = TenantStatus.SUSPENDED;
		this.updatedAt = OffsetDateTime.now();
	}
	
	public void activate() {
		this.status = TenantStatus.ACTIVE;
		this.updatedAt = OffsetDateTime.now();
	}
}
