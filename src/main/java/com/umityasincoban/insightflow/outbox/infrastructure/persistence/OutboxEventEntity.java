package com.umityasincoban.insightflow.outbox.infrastructure.persistence;

import com.umityasincoban.insightflow.outbox.domain.OutboxEventStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "outbox_events")
public class OutboxEventEntity {
	
	@Id
	private UUID id;
	
	@Column(name = "tenant_id")
	private UUID tenantId;
	
	@Column(name = "aggregate_type", nullable = false, length = 120)
	private String aggregateType;
	
	@Column(name = "aggregate_id", nullable = false)
	private UUID aggregateId;
	
	@Column(name = "event_type", nullable = false, length = 160)
	private String eventType;
	
	@Column(name = "event_version", nullable = false)
	private int eventVersion;
	
	@JdbcTypeCode(SqlTypes.JSON)
	@Column(name = "payload", nullable = false, columnDefinition = "jsonb")
	private Map<String, Object> payload;
	
	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false, length = 40)
	private OutboxEventStatus status;
	
	@Column(name = "retry_count", nullable = false)
	private int retryCount;
	
	@Column(name = "next_retry_at")
	private OffsetDateTime nextRetryAt;
	
	@Column(name = "last_error", columnDefinition = "text")
	private String lastError;
	
	@Column(name = "created_at", nullable = false)
	private OffsetDateTime createdAt;
	
	@Column(name = "published_at")
	private OffsetDateTime publishedAt;
	
	protected OutboxEventEntity() {
	}
	
	public OutboxEventEntity(
			UUID id,
			UUID tenantId,
			String aggregateType,
			UUID aggregateId,
			String eventType,
			int eventVersion,
			Map<String, Object> payload
	) {
		this.id = id;
		this.tenantId = tenantId;
		this.aggregateType = aggregateType;
		this.aggregateId = aggregateId;
		this.eventType = eventType;
		this.eventVersion = eventVersion;
		this.payload = payload == null ? Map.of() : Map.copyOf(payload);
		this.status = OutboxEventStatus.PENDING;
		this.retryCount = 0;
		this.createdAt = OffsetDateTime.now();
	}
	
	public UUID getId() {
		return id;
	}
	
	public UUID getTenantId() {
		return tenantId;
	}
	
	public String getAggregateType() {
		return aggregateType;
	}
	
	public UUID getAggregateId() {
		return aggregateId;
	}
	
	public String getEventType() {
		return eventType;
	}
	
	public int getEventVersion() {
		return eventVersion;
	}
	
	public Map<String, Object> getPayload() {
		return payload;
	}
	
	public OutboxEventStatus getStatus() {
		return status;
	}
	
	public int getRetryCount() {
		return retryCount;
	}
	
	public OffsetDateTime getNextRetryAt() {
		return nextRetryAt;
	}
	
	public String getLastError() {
		return lastError;
	}
	
	public OffsetDateTime getCreatedAt() {
		return createdAt;
	}
	
	public OffsetDateTime getPublishedAt() {
		return publishedAt;
	}
}