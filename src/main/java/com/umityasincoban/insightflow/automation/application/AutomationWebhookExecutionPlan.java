package com.umityasincoban.insightflow.automation.application;

import java.util.Map;

public record AutomationWebhookExecutionPlan(
		AutomationWebhookRequest request,
		Map<String, Object> requestPayload
) {
}
