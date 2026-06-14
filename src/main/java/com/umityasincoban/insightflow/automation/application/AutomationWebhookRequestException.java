package com.umityasincoban.insightflow.automation.application;

import java.util.Map;

public class AutomationWebhookRequestException extends RuntimeException {
	
	private final Map<String, Object> resultPayload;
	
	public AutomationWebhookRequestException(String message, Map<String, Object> resultPayload) {
		super(message);
		this.resultPayload = resultPayload == null ? Map.of() : Map.copyOf(resultPayload);
	}
	
	public Map<String, Object> getResultPayload() {
		return resultPayload;
	}
}
