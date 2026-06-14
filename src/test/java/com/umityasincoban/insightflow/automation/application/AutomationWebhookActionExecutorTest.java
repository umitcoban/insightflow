package com.umityasincoban.insightflow.automation.application;

import com.umityasincoban.insightflow.automation.domain.AutomationRuleId;
import com.umityasincoban.insightflow.outbox.application.OutboxEventMessage;
import com.umityasincoban.insightflow.tenancy.domain.TenantId;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AutomationWebhookActionExecutorTest {
	
	private final FakeWebhookClient webhookClient = new FakeWebhookClient();
	private final AutomationWebhookProperties properties = properties();
	private final AutomationWebhookActionExecutor executor = new AutomationWebhookActionExecutor(
			webhookClient,
			new AutomationWebhookUrlValidator(),
			new AutomationWebhookTemplateResolver(),
			new AutomationWebhookPayloadSanitizer(),
			properties
	);
	
	@Test
	void twoHundredResponseProducesSuccess() {
		webhookClient.response = new AutomationWebhookResponse(200, "ok", 12);
		
		AutomationActionExecutionResult result = executor.execute(
				tenantId(),
				AutomationRuleId.of(UUID.randomUUID()),
				UUID.randomUUID(),
				message(),
				action()
		);
		
		assertTrue(result.success());
		assertEquals(200, result.resultPayload().get("httpStatus"));
	}
	
	@Test
	void nonTwoHundredResponseProducesFailed() {
		webhookClient.response = new AutomationWebhookResponse(500, "error", 20);
		
		AutomationActionExecutionResult result = executor.execute(
				tenantId(),
				AutomationRuleId.of(UUID.randomUUID()),
				UUID.randomUUID(),
				message(),
				action()
		);
		
		assertFalse(result.success());
		assertEquals(500, result.resultPayload().get("httpStatus"));
	}
	
	@Test
	void timeoutProducesFailed() {
		webhookClient.exception = new AutomationWebhookRequestException(
				"Webhook request failed: TIMEOUT",
				Map.of("success", false, "errorType", "TIMEOUT", "durationMs", 101)
		);
		
		AutomationActionExecutionResult result = executor.execute(
				tenantId(),
				AutomationRuleId.of(UUID.randomUUID()),
				UUID.randomUUID(),
				message(),
				action()
		);
		
		assertFalse(result.success());
		assertEquals("TIMEOUT", result.resultPayload().get("errorType"));
	}
	
	@Test
	void responseBodyIsTruncated() {
		webhookClient.response = new AutomationWebhookResponse(200, "0123456789", 12);
		
		AutomationActionExecutionResult result = executor.execute(
				tenantId(),
				AutomationRuleId.of(UUID.randomUUID()),
				UUID.randomUUID(),
				message(),
				action()
		);
		
		assertEquals("01234", result.resultPayload().get("responseBody"));
		assertEquals(true, result.resultPayload().get("responseBodyTruncated"));
	}
	
	@Test
	@SuppressWarnings("unchecked")
	void sensitiveHeadersAreNotPersistedUnmasked() {
		webhookClient.response = new AutomationWebhookResponse(200, "ok", 12);
		
		AutomationActionExecutionResult result = executor.execute(
				tenantId(),
				AutomationRuleId.of(UUID.randomUUID()),
				UUID.randomUUID(),
				message(),
				action()
		);
		
		Map<String, String> headers = (Map<String, String>) result.requestPayload().get("headers");
		assertEquals("***", headers.get("Authorization"));
		assertEquals("InsightFlow", headers.get("X-Source"));
	}
	
	private static TenantId tenantId() {
		return TenantId.of(UUID.randomUUID());
	}
	
	private static Map<String, Object> action() {
		return Map.of(
				"type", "WEBHOOK",
				"url", "https://example.com/webhooks/feedback",
				"method", "POST",
				"headers", Map.of("Authorization", "Bearer secret", "X-Source", "InsightFlow"),
				"body", Map.of("riskLevel", "{{payload.riskLevel}}"),
				"timeoutMs", 5000
		);
	}
	
	private static OutboxEventMessage message() {
		return new OutboxEventMessage(
				UUID.randomUUID().toString(),
				UUID.randomUUID().toString(),
				"FEEDBACK",
				UUID.randomUUID().toString(),
				"feedback.ai-analysis-completed",
				1,
				Map.of("riskLevel", "CHURN_RISK"),
				OffsetDateTime.now().toString()
		);
	}
	
	private static AutomationWebhookProperties properties() {
		AutomationWebhookProperties properties = new AutomationWebhookProperties();
		properties.setMaxResponseBodyLength(5);
		return properties;
	}
	
	private static final class FakeWebhookClient implements AutomationWebhookClient {
		
		private AutomationWebhookResponse response = new AutomationWebhookResponse(200, "ok", 1);
		private AutomationWebhookRequestException exception;
		
		@Override
		public AutomationWebhookResponse send(AutomationWebhookRequest request) {
			if (exception != null) {
				throw exception;
			}
			return response;
		}
	}
}
