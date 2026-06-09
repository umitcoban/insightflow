package com.umityasincoban.insightflow.automation.infrastructure.persistence;

import com.umityasincoban.insightflow.automation.domain.AutomationExecutionStatus;
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
@Table(name = "automation_action_executions")
public class AutomationActionExecutionEntity {
	
	@Id
	private UUID id;
	
	@Column(name = "tenant_id", nullable = false)
	private UUID tenantId;
	
	@Column(name = "execution_id", nullable = false)
	private UUID executionId;
	
	@Column(name = "action_type", nullable = false, length = 80)
	private String actionType;
	
	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false, length = 40)
	private AutomationExecutionStatus status;
	
	@JdbcTypeCode(SqlTypes.JSON)
	@Column(name = "request_payload", nullable = false, columnDefinition = "jsonb")
	private Map<String, Object> requestPayload;
	
	@JdbcTypeCode(SqlTypes.JSON)
	@Column(name = "result_payload", nullable = false, columnDefinition = "jsonb")
	private Map<String, Object> resultPayload;
	
	@Column(name = "error_message", columnDefinition = "text")
	private String errorMessage;
	
	@Column(name = "created_at", nullable = false)
	private OffsetDateTime createdAt;
	
	protected AutomationActionExecutionEntity() {
	}
	
	private AutomationActionExecutionEntity(
			UUID tenantId,
			UUID executionId,
			String actionType,
			AutomationExecutionStatus status,
			Map<String, Object> requestPayload,
			Map<String, Object> resultPayload,
			String errorMessage
	) {
		this.id = UUID.randomUUID();
		this.tenantId = tenantId;
		this.executionId = executionId;
		this.actionType = actionType;
		this.status = status;
		this.requestPayload = requestPayload == null ? Map.of() : Map.copyOf(requestPayload);
		this.resultPayload = resultPayload == null ? Map.of() : Map.copyOf(resultPayload);
		this.errorMessage = errorMessage;
		this.createdAt = OffsetDateTime.now();
	}
	
	public static AutomationActionExecutionEntity success(
			UUID tenantId,
			UUID executionId,
			String actionType,
			Map<String, Object> requestPayload,
			Map<String, Object> resultPayload
	) {
		return new AutomationActionExecutionEntity(
				tenantId,
				executionId,
				actionType,
				AutomationExecutionStatus.SUCCESS,
				requestPayload,
				resultPayload,
				null
		);
	}
	
	public static AutomationActionExecutionEntity failed(
			UUID tenantId,
			UUID executionId,
			String actionType,
			Map<String, Object> requestPayload,
			String errorMessage
	) {
		return new AutomationActionExecutionEntity(
				tenantId,
				executionId,
				actionType,
				AutomationExecutionStatus.FAILED,
				requestPayload,
				Map.of(),
				errorMessage
		);
	}
	
	public UUID getId() {
		return id;
	}
	
	public UUID getTenantId() {
		return tenantId;
	}
	
	public UUID getExecutionId() {
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