package com.umityasincoban.insightflow.automation.api;

import com.umityasincoban.insightflow.automation.domain.AutomationActionExecution;
import com.umityasincoban.insightflow.automation.domain.AutomationExecutionStatus;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

public record AutomationActionExecutionResponse(
		UUID id,
		UUID tenantId,
		UUID executionId,
		String actionType,
		AutomationExecutionStatus status,
		Map<String, Object> requestPayload,
		Map<String, Object> resultPayload,
		String errorMessage,
		int attemptCount,
		int maxAttempts,
		OffsetDateTime nextRetryAt,
		OffsetDateTime lastAttemptAt,
		OffsetDateTime completedAt,
		OffsetDateTime createdAt
) {
	
	public static AutomationActionExecutionResponse from(AutomationActionExecution actionExecution) {
		return new AutomationActionExecutionResponse(
				actionExecution.getId().value(),
				actionExecution.getTenantId().value(),
				actionExecution.getExecutionId().value(),
				actionExecution.getActionType(),
				actionExecution.getStatus(),
				actionExecution.getRequestPayload(),
				actionExecution.getResultPayload(),
				actionExecution.getErrorMessage(),
				actionExecution.getAttemptCount(),
				actionExecution.getMaxAttempts(),
				actionExecution.getNextRetryAt(),
				actionExecution.getLastAttemptAt(),
				actionExecution.getCompletedAt(),
				actionExecution.getCreatedAt()
		);
	}
}
