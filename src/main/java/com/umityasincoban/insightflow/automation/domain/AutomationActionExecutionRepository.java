package com.umityasincoban.insightflow.automation.domain;

import com.umityasincoban.insightflow.tenancy.domain.TenantId;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface AutomationActionExecutionRepository {
	
	AutomationActionExecution savePending(
			TenantId tenantId,
			AutomationExecutionId executionId,
			String actionType,
			Map<String, Object> requestPayload,
			int maxAttempts
	);
	
	AutomationActionExecution markInProgress(
			TenantId tenantId,
			AutomationActionExecutionId actionExecutionId,
			OffsetDateTime lastAttemptAt
	);
	
	AutomationActionExecution markSuccess(
			TenantId tenantId,
			AutomationActionExecutionId actionExecutionId,
			Map<String, Object> resultPayload,
			OffsetDateTime completedAt
	);
	
	AutomationActionExecution markRetryScheduled(
			TenantId tenantId,
			AutomationActionExecutionId actionExecutionId,
			Map<String, Object> resultPayload,
			String errorMessage,
			OffsetDateTime nextRetryAt
	);
	
	AutomationActionExecution markFailed(
			TenantId tenantId,
			AutomationActionExecutionId actionExecutionId,
			Map<String, Object> resultPayload,
			String errorMessage,
			OffsetDateTime completedAt
	);
	
	AutomationActionExecution saveSuccess(
			TenantId tenantId,
			AutomationExecutionId executionId,
			String actionType,
			Map<String, Object> requestPayload,
			Map<String, Object> resultPayload
	);
	
	AutomationActionExecution saveFailed(
			TenantId tenantId,
			AutomationExecutionId executionId,
			String actionType,
			Map<String, Object> requestPayload,
			Map<String, Object> resultPayload,
			String errorMessage
	);
	
	List<AutomationActionExecution> findByTenantIdAndExecutionIdOrderByCreatedAtAsc(
			TenantId tenantId,
			AutomationExecutionId executionId
	);
	
	Optional<AutomationActionExecution> findByTenantIdAndId(TenantId tenantId, AutomationActionExecutionId actionExecutionId);
	
	List<AutomationActionExecution> claimDueWebhookRetries(
			int limit,
			OffsetDateTime now,
			OffsetDateTime staleBefore
	);
	
	List<AutomationActionExecution> findByExecutionId(UUID executionId);
}
