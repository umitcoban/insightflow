package com.umityasincoban.insightflow.automation.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.Map;

public record CreateAutomationRuleRequest(
		@NotBlank(message = "Automation rule name is required")
		@Size(max = 160, message = "Automation rule name cannot be longer than 160 characters")
		String name,
		
		String description,
		
		@NotBlank(message = "Automation rule trigger event type is required")
		@Size(max = 160, message = "Automation rule trigger event type cannot be longer than 160 characters")
		String triggerEventType,
		
		@NotNull(message = "Automation rule condition is required")
		Map<String, Object> conditionJson,
		
		@NotEmpty(message = "Automation rule actions are required")
		List<Map<String, Object>> actionJson,
		
		Integer priority
) {
}
