package com.umityasincoban.insightflow.automation.domain;

import java.util.Objects;
import java.util.UUID;

public record AutomationRuleId(UUID value) {
	
	public AutomationRuleId {
		Objects.requireNonNull(value, "Automation rule id cannot be null");
	}
	
	public static AutomationRuleId of(UUID value) {
		return new AutomationRuleId(value);
	}
}
