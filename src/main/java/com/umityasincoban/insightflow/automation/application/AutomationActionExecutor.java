package com.umityasincoban.insightflow.automation.application;

import com.umityasincoban.insightflow.automation.domain.AutomationRuleId;
import com.umityasincoban.insightflow.outbox.application.OutboxEventMessage;
import com.umityasincoban.insightflow.tenancy.domain.TenantId;

import java.util.Map;
import java.util.UUID;

public interface AutomationActionExecutor {
	
	boolean supports(String actionType);
	
	AutomationActionExecutionResult execute(
			TenantId tenantId,
			AutomationRuleId ruleId,
			UUID sourceEventId,
			OutboxEventMessage eventMessage,
			Map<String, Object> actionJson
	);
}
