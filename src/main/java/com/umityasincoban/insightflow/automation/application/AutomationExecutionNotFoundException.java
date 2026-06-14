package com.umityasincoban.insightflow.automation.application;

import java.util.UUID;

public class AutomationExecutionNotFoundException extends RuntimeException {
	
	public AutomationExecutionNotFoundException(UUID executionId) {
		super("Automation execution not found: " + executionId);
	}
}
