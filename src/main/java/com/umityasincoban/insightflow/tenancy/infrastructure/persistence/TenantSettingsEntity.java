package com.umityasincoban.insightflow.tenancy.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "tenant_settings")
public class TenantSettingsEntity {
	
	@Id
	@Column(name = "tenant_id", nullable = false)
	private UUID tenantId;
	
	@JdbcTypeCode(SqlTypes.JSON)
	@Column(name = "settings", nullable = false, columnDefinition = "jsonb")
	private Map<String, Object> settings;
	
	@Column(name = "updated_at", nullable = false)
	private OffsetDateTime updatedAt;
	
	protected TenantSettingsEntity() {
	}
	
	public TenantSettingsEntity(UUID tenantId, Map<String, Object> settings) {
		this.tenantId = tenantId;
		this.settings = settings == null ? Map.of() : Map.copyOf(settings);
		this.updatedAt = OffsetDateTime.now();
	}
	
	public UUID getTenantId() {
		return tenantId;
	}
	
	public Map<String, Object> getSettings() {
		return settings;
	}
	
	public OffsetDateTime getUpdatedAt() {
		return updatedAt;
	}
	
	public void replaceSettings(Map<String, Object> settings) {
		this.settings = settings == null ? Map.of() : Map.copyOf(settings);
		this.updatedAt = OffsetDateTime.now();
	}
}

