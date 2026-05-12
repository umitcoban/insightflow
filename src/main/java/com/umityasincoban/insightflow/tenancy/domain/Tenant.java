package com.umityasincoban.insightflow.tenancy.domain;

import java.time.OffsetDateTime;
import java.util.Objects;

public class Tenant {
	private final TenantId id;
	private final String slug;
	private final String name;
	private final TenantStatus status;
	private final OffsetDateTime createdAt;
	private final OffsetDateTime updatedAt;
	
	public Tenant(TenantId id, String slug, String name, TenantStatus status, OffsetDateTime createdAt, OffsetDateTime updatedAt) {
		this.id = Objects.requireNonNull(id, "Tenant id cannot be null");
		this.slug = validateSlug(slug);
		this.name = name;
		this.status = Objects.requireNonNull(status, "Tenant status cannot be null");
		this.createdAt = Objects.requireNonNull(createdAt, "Tenant createdAt cannot be null");
		this.updatedAt = Objects.requireNonNull(updatedAt, "Tenant updatedAt cannot be null");
	}
	
	public TenantId getId() {
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
	
	public boolean isActive() {
		return TenantStatus.ACTIVE.equals(status);
	}
	
	public OffsetDateTime getCreatedAt() {
		return createdAt;
	}
	
	public OffsetDateTime getUpdatedAt() {
		return updatedAt;
	}
	
	private static String validateSlug(String slug) {
		
		if (slug == null || slug.isBlank()) {
			throw new IllegalArgumentException("Tenant slug cannot be null or blank");
		}
		
		if (slug.length() > 80) {
			throw new IllegalArgumentException("Tenant slug cannot be longer than 80 characters");
		}
		return slug.toLowerCase().replaceAll("[^a-z0-9-]", "-");
	}
	
	private static String validateName(String name) {
		if(name == null || name.isBlank()) {
			throw new IllegalArgumentException("Tenant name cannot be null or blank");
		}
		if(name.length() > 160) {
			throw new IllegalArgumentException("Tenant name cannot be longer than 160 characters");
		}
		return name.trim();
	}
	
}
