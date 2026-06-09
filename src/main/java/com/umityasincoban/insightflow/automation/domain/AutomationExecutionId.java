package com.umityasincoban.insightflow.automation.domain;

import java.util.Objects;
import java.util.UUID;

public record AutomationExecutionId(UUID value) {
	
	public AutomationExecutionId {
		Objects.requireNonNull(value, "Automation execution id cannot be null");
	}
	
	public static AutomationExecutionId of(UUID value) {
		return new AutomationExecutionId(value);
	}
}
