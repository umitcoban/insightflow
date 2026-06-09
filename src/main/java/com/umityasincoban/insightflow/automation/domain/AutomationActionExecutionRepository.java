package com.umityasincoban.insightflow.automation.domain;

import com.umityasincoban.insightflow.tenancy.domain.TenantId;

import java.util.Map;

public interface AutomationActionExecutionRepository {
	
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
			String errorMessage
	);
}
