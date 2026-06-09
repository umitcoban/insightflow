package com.umityasincoban.insightflow.automation.infrastructure.persistence;

import com.umityasincoban.insightflow.automation.domain.AutomationExecutionStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "automation_executions")
public class AutomationExecutionEntity {
	
	@Id
	private UUID id;
	
	@Column(name = "tenant_id", nullable = false)
	private UUID tenantId;
	
	@Column(name = "rule_id", nullable = false)
	private UUID ruleId;
	
	@Column(name = "source_event_id", nullable = false)
	private UUID sourceEventId;
	
	@Column(name = "source_event_type", nullable = false, length = 160)
	private String sourceEventType;
	
	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false, length = 40)
	private AutomationExecutionStatus status;
	
	@Column(name = "matched", nullable = false)
	private boolean matched;
	
	@Column(name = "error_message", columnDefinition = "text")
	private String errorMessage;
	
	@Column(name = "started_at", nullable = false)
	private OffsetDateTime startedAt;
	
	@Column(name = "finished_at")
	private OffsetDateTime finishedAt;
	
	protected AutomationExecutionEntity() {
	}
	
	public AutomationExecutionEntity(
			UUID tenantId,
			UUID ruleId,
			UUID sourceEventId,
			String sourceEventType
	) {
		this.id = UUID.randomUUID();
		this.tenantId = tenantId;
		this.ruleId = ruleId;
		this.sourceEventId = sourceEventId;
		this.sourceEventType = sourceEventType;
		this.status = AutomationExecutionStatus.SKIPPED;
		this.matched = false;
		this.startedAt = OffsetDateTime.now();
	}
	
	public UUID getId() {
		return id;
	}
	
	public UUID getTenantId() {
		return tenantId;
	}
	
	public UUID getRuleId() {
		return ruleId;
	}
	
	public UUID getSourceEventId() {
		return sourceEventId;
	}
	
	public String getSourceEventType() {
		return sourceEventType;
	}
	
	public AutomationExecutionStatus getStatus() {
		return status;
	}
	
	public boolean isMatched() {
		return matched;
	}
	
	public String getErrorMessage() {
		return errorMessage;
	}
	
	public OffsetDateTime getStartedAt() {
		return startedAt;
	}
	
	public OffsetDateTime getFinishedAt() {
		return finishedAt;
	}
	
	public void markSkipped() {
		this.status = AutomationExecutionStatus.SKIPPED;
		this.matched = false;
		this.finishedAt = OffsetDateTime.now();
	}
	
	public void markSuccess(boolean matched) {
		this.status = AutomationExecutionStatus.SUCCESS;
		this.matched = matched;
		this.finishedAt = OffsetDateTime.now();
	}
	
	public void markFailed(String errorMessage) {
		this.status = AutomationExecutionStatus.FAILED;
		this.errorMessage = errorMessage;
		this.finishedAt = OffsetDateTime.now();
	}
	
	public void markFailed(boolean matched, String errorMessage) {
		this.status = AutomationExecutionStatus.FAILED;
		this.matched = matched;
		this.errorMessage = errorMessage;
		this.finishedAt = OffsetDateTime.now();
	}
}
