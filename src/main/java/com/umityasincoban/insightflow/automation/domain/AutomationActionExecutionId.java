package com.umityasincoban.insightflow.automation.domain;

import java.util.Objects;
import java.util.UUID;

public record AutomationActionExecutionId(UUID value) {
	
	public AutomationActionExecutionId {
		Objects.requireNonNull(value, "Automation action execution id cannot be null");
	}
	
	public static AutomationActionExecutionId of(UUID value) {
		return new AutomationActionExecutionId(value);
	}
}
