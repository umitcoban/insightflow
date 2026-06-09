package com.umityasincoban.insightflow.automation.domain;

import com.umityasincoban.insightflow.tenancy.domain.TenantId;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Objects;

public class AutomationActionExecution {
	
	private final AutomationActionExecutionId id;
	private final TenantId tenantId;
	private final AutomationExecutionId executionId;
	private final String actionType;
	private final AutomationExecutionStatus status;
	private final Map<String, Object> requestPayload;
	private final Map<String, Object> resultPayload;
	private final String errorMessage;
	private final OffsetDateTime createdAt;
	
	public AutomationActionExecution(
			AutomationActionExecutionId id,
			TenantId tenantId,
			AutomationExecutionId executionId,
			String actionType,
			AutomationExecutionStatus status,
			Map<String, Object> requestPayload,
			Map<String, Object> resultPayload,
			String errorMessage,
			OffsetDateTime createdAt
	) {
		this.id = Objects.requireNonNull(id, "Automation action execution id cannot be null");
		this.tenantId = Objects.requireNonNull(tenantId, "Tenant id cannot be null");
		this.executionId = Objects.requireNonNull(executionId, "Automation execution id cannot be null");
		this.actionType = Objects.requireNonNull(actionType, "Automation action type cannot be null");
		this.status = Objects.requireNonNull(status, "Automation action execution status cannot be null");
		this.requestPayload = Map.copyOf(Objects.requireNonNull(requestPayload, "Automation action request payload cannot be null"));
		this.resultPayload = Map.copyOf(Objects.requireNonNull(resultPayload, "Automation action result payload cannot be null"));
		this.errorMessage = errorMessage;
		this.createdAt = Objects.requireNonNull(createdAt, "Automation action execution createdAt cannot be null");
	}
	
	public AutomationActionExecutionId getId() {
		return id;
	}
	
	public TenantId getTenantId() {
		return tenantId;
	}
	
	public AutomationExecutionId getExecutionId() {
		return executionId;
	}
	
	public String getActionType() {
		return actionType;
	}
	
	public AutomationExecutionStatus getStatus() {
		return status;
	}
	
	public Map<String, Object> getRequestPayload() {
		return requestPayload;
	}
	
	public Map<String, Object> getResultPayload() {
		return resultPayload;
	}
	
	public String getErrorMessage() {
		return errorMessage;
	}
	
	public OffsetDateTime getCreatedAt() {
		return createdAt;
	}
}
