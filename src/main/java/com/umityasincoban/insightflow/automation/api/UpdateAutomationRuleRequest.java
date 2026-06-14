package com.umityasincoban.insightflow.automation.api;

import java.util.List;
import java.util.Map;

public record UpdateAutomationRuleRequest(
		String name,
		String description,
		String triggerEventType,
		Map<String, Object> conditionJson,
		List<Map<String, Object>> actionJson,
		Integer priority
) {
}
