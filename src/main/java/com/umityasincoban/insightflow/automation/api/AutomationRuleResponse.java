package com.umityasincoban.insightflow.automation.api;

import com.umityasincoban.insightflow.automation.domain.AutomationRule;
import com.umityasincoban.insightflow.automation.domain.AutomationRuleStatus;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record AutomationRuleResponse(
		UUID id,
		UUID tenantId,
		String name,
		String description,
		String triggerEventType,
		Map<String, Object> conditionJson,
		List<Map<String, Object>> actionJson,
		AutomationRuleStatus status,
		int priority,
		OffsetDateTime createdAt,
		OffsetDateTime updatedAt
) {
	
	public static AutomationRuleResponse from(AutomationRule automationRule) {
		return new AutomationRuleResponse(
				automationRule.getId().value(),
				automationRule.getTenantId().value(),
				automationRule.getName(),
				automationRule.getDescription(),
				automationRule.getTriggerEventType(),
				automationRule.getConditionJson(),
				automationRule.getActionJson(),
				automationRule.getStatus(),
				automationRule.getPriority(),
				automationRule.getCreatedAt(),
				automationRule.getUpdatedAt()
		);
	}
}
