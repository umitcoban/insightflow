package com.umityasincoban.insightflow.automation.application;

import java.util.Map;

public record AutomationActionExecutionResult(
		boolean success,
		String actionType,
		Map<String, Object> resultPayload,
		String errorMessage
) {
	
	public static AutomationActionExecutionResult success(String actionType, Map<String, Object> resultPayload) {
		return new AutomationActionExecutionResult(true, actionType, resultPayload, null);
	}
	
	public static AutomationActionExecutionResult failed(String actionType, String errorMessage) {
		return new AutomationActionExecutionResult(false, actionType, Map.of(), errorMessage);
	}
}
