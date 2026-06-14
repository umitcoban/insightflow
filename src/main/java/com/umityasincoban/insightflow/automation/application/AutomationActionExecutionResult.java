package com.umityasincoban.insightflow.automation.application;

import java.util.Map;

public record AutomationActionExecutionResult(
		boolean success,
		String actionType,
		Map<String, Object> requestPayload,
		Map<String, Object> resultPayload,
		String errorMessage
) {
	
	public static AutomationActionExecutionResult success(String actionType, Map<String, Object> resultPayload) {
		return success(actionType, Map.of(), resultPayload);
	}
	
	public static AutomationActionExecutionResult success(
			String actionType,
			Map<String, Object> requestPayload,
			Map<String, Object> resultPayload
	) {
		return new AutomationActionExecutionResult(true, actionType, requestPayload, resultPayload, null);
	}
	
	public static AutomationActionExecutionResult failed(String actionType, String errorMessage) {
		return failed(actionType, Map.of(), Map.of(), errorMessage);
	}
	
	public static AutomationActionExecutionResult failed(
			String actionType,
			Map<String, Object> resultPayload,
			String errorMessage
	) {
		return failed(actionType, Map.of(), resultPayload, errorMessage);
	}
	
	public static AutomationActionExecutionResult failed(
			String actionType,
			Map<String, Object> requestPayload,
			Map<String, Object> resultPayload,
			String errorMessage
	) {
		return new AutomationActionExecutionResult(false, actionType, requestPayload, resultPayload, errorMessage);
	}
}
