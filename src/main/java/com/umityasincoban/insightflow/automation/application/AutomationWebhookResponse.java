package com.umityasincoban.insightflow.automation.application;

public record AutomationWebhookResponse(
		int httpStatus,
		String responseBody,
		long durationMs
) {
	
	public boolean success() {
		return httpStatus >= 200 && httpStatus <= 299;
	}
}
