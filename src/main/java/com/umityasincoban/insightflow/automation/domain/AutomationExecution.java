package com.umityasincoban.insightflow.automation.domain;

import com.umityasincoban.insightflow.tenancy.domain.TenantId;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

public class AutomationExecution {
	
	private final AutomationExecutionId id;
	private final TenantId tenantId;
	private final AutomationRuleId ruleId;
	private final UUID sourceEventId;
	private final String sourceEventType;
	private final AutomationExecutionStatus status;
	private final boolean matched;
	private final String errorMessage;
	private final OffsetDateTime startedAt;
	private final OffsetDateTime finishedAt;
	
	public AutomationExecution(
			AutomationExecutionId id,
			TenantId tenantId,
			AutomationRuleId ruleId,
			UUID sourceEventId,
			String sourceEventType,
			AutomationExecutionStatus status,
			boolean matched,
			String errorMessage,
			OffsetDateTime startedAt,
			OffsetDateTime finishedAt
	) {
		this.id = Objects.requireNonNull(id, "Automation execution id cannot be null");
		this.tenantId = Objects.requireNonNull(tenantId, "Tenant id cannot be null");
		this.ruleId = Objects.requireNonNull(ruleId, "Automation rule id cannot be null");
		this.sourceEventId = Objects.requireNonNull(sourceEventId, "Source event id cannot be null");
		this.sourceEventType = Objects.requireNonNull(sourceEventType, "Source event type cannot be null");
		this.status = Objects.requireNonNull(status, "Automation execution status cannot be null");
		this.matched = matched;
		this.errorMessage = errorMessage;
		this.startedAt = Objects.requireNonNull(startedAt, "Automation execution startedAt cannot be null");
		this.finishedAt = finishedAt;
	}
	
	public AutomationExecutionId getId() {
		return id;
	}
	
	public TenantId getTenantId() {
		return tenantId;
	}
	
	public AutomationRuleId getRuleId() {
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
}
