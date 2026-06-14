package com.umityasincoban.insightflow.automation.application;

public record AutomationWebhookResponse(
		int httpStatus,
		String responseBody,
		long durationMs,
		String retryAfter
) {
	
	public AutomationWebhookResponse(int httpStatus, String responseBody, long durationMs) {
		this(httpStatus, responseBody, durationMs, null);
	}
	
	public boolean success() {
		return httpStatus >= 200 && httpStatus <= 299;
	}
}
