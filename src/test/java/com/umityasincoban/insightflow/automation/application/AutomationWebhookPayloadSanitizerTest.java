package com.umityasincoban.insightflow.automation.application;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AutomationWebhookPayloadSanitizerTest {
	
	private final AutomationWebhookPayloadSanitizer sanitizer = new AutomationWebhookPayloadSanitizer();
	
	@Test
	@SuppressWarnings("unchecked")
	void masksSensitiveHeadersAndPreservesNormalHeaders() {
		Map<String, Object> payload = sanitizer.sanitizeRequestPayload(
				"https://example.com/webhook",
				"POST",
				Map.of(
						"Authorization", "Bearer secret",
						"X-API-Key", "api-secret",
						"X-Source", "InsightFlow"
				),
				Map.of("ok", true),
				5000
		);
		
		Map<String, String> headers = (Map<String, String>) payload.get("headers");
		assertEquals("***", headers.get("Authorization"));
		assertEquals("***", headers.get("X-API-Key"));
		assertEquals("InsightFlow", headers.get("X-Source"));
	}
	
	@Test
	void matchesSensitiveHeadersCaseInsensitively() {
		Map<String, String> headers = sanitizer.sanitizeHeaders(Map.of("authorization", "secret"));
		
		assertEquals("***", headers.get("authorization"));
	}
}
