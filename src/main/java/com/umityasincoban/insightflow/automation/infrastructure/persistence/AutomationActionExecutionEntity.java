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
	
	@Column(name = "attempt_count", nullable = false)
	private int attemptCount;
	
	@Column(name = "max_attempts", nullable = false)
	private int maxAttempts;
	
	@Column(name = "next_retry_at")
	private OffsetDateTime nextRetryAt;
	
	@Column(name = "last_attempt_at")
	private OffsetDateTime lastAttemptAt;
	
	@Column(name = "completed_at")
	private OffsetDateTime completedAt;
	
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
			String errorMessage,
			int attemptCount,
			int maxAttempts
	) {
		this.id = UUID.randomUUID();
		this.tenantId = tenantId;
		this.executionId = executionId;
		this.actionType = actionType;
		this.status = status;
		this.requestPayload = requestPayload == null ? Map.of() : Map.copyOf(requestPayload);
		this.resultPayload = resultPayload == null ? Map.of() : Map.copyOf(resultPayload);
		this.errorMessage = errorMessage;
		this.attemptCount = attemptCount;
		this.maxAttempts = maxAttempts;
		if (status == AutomationExecutionStatus.SUCCESS || status == AutomationExecutionStatus.FAILED) {
			this.completedAt = OffsetDateTime.now();
		}
		this.createdAt = OffsetDateTime.now();
	}
	
	public static AutomationActionExecutionEntity pending(
			UUID tenantId,
			UUID executionId,
			String actionType,
			Map<String, Object> requestPayload,
			int maxAttempts
	) {
		return new AutomationActionExecutionEntity(
				tenantId,
				executionId,
				actionType,
				AutomationExecutionStatus.PENDING,
				requestPayload,
				Map.of(),
				null,
				0,
				maxAttempts
		);
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
				null,
				1,
				1
		);
	}
	
	public static AutomationActionExecutionEntity failed(
			UUID tenantId,
			UUID executionId,
			String actionType,
			Map<String, Object> requestPayload,
			Map<String, Object> resultPayload,
			String errorMessage
	) {
		return new AutomationActionExecutionEntity(
				tenantId,
				executionId,
				actionType,
				AutomationExecutionStatus.FAILED,
				requestPayload,
				resultPayload,
				errorMessage,
				1,
				1
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
	
	public int getAttemptCount() {
		return attemptCount;
	}
	
	public int getMaxAttempts() {
		return maxAttempts;
	}
	
	public OffsetDateTime getNextRetryAt() {
		return nextRetryAt;
	}
	
	public OffsetDateTime getLastAttemptAt() {
		return lastAttemptAt;
	}
	
	public OffsetDateTime getCompletedAt() {
		return completedAt;
	}
	
	public OffsetDateTime getCreatedAt() {
		return createdAt;
	}
	
	public void markInProgress(OffsetDateTime lastAttemptAt) {
		boolean alreadyInProgress = this.status == AutomationExecutionStatus.IN_PROGRESS;
		this.status = AutomationExecutionStatus.IN_PROGRESS;
		if (!alreadyInProgress) {
			this.attemptCount = this.attemptCount + 1;
		}
		this.lastAttemptAt = lastAttemptAt;
		this.nextRetryAt = null;
	}
	
	public void markSuccess(Map<String, Object> resultPayload, OffsetDateTime completedAt) {
		this.status = AutomationExecutionStatus.SUCCESS;
		this.resultPayload = resultPayload == null ? Map.of() : Map.copyOf(resultPayload);
		this.errorMessage = null;
		this.nextRetryAt = null;
		this.completedAt = completedAt;
	}
	
	public void markRetryScheduled(Map<String, Object> resultPayload, String errorMessage, OffsetDateTime nextRetryAt) {
		this.status = AutomationExecutionStatus.RETRY_SCHEDULED;
		this.resultPayload = resultPayload == null ? Map.of() : Map.copyOf(resultPayload);
		this.errorMessage = errorMessage;
		this.nextRetryAt = nextRetryAt;
	}
	
	public void markFailed(Map<String, Object> resultPayload, String errorMessage, OffsetDateTime completedAt) {
		this.status = AutomationExecutionStatus.FAILED;
		this.resultPayload = resultPayload == null ? Map.of() : Map.copyOf(resultPayload);
		this.errorMessage = errorMessage;
		this.nextRetryAt = null;
		this.completedAt = completedAt;
	}
}
