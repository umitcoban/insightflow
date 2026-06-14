package com.umityasincoban.insightflow.automation.application;

import java.net.URI;
import java.util.Map;

public record AutomationWebhookRequest(
		URI url,
		String method,
		Map<String, String> headers,
		Object body,
		int timeoutMs
) {
}
