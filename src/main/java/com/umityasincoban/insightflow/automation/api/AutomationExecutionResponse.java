package com.umityasincoban.insightflow.automation.api;

import com.umityasincoban.insightflow.automation.domain.AutomationExecution;
import com.umityasincoban.insightflow.automation.domain.AutomationExecutionStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

public record AutomationExecutionResponse(
		UUID id,
		UUID tenantId,
		UUID ruleId,
		UUID sourceEventId,
		String sourceEventType,
		AutomationExecutionStatus status,
		boolean matched,
		String errorMessage,
		OffsetDateTime startedAt,
		OffsetDateTime finishedAt
) {
	
	public static AutomationExecutionResponse from(AutomationExecution execution) {
		return new AutomationExecutionResponse(
				execution.getId().value(),
				execution.getTenantId().value(),
				execution.getRuleId().value(),
				execution.getSourceEventId(),
				execution.getSourceEventType(),
				execution.getStatus(),
				execution.isMatched(),
				execution.getErrorMessage(),
				execution.getStartedAt(),
				execution.getFinishedAt()
		);
	}
}
