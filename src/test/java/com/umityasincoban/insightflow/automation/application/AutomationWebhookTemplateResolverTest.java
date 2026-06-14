package com.umityasincoban.insightflow.automation.application;

import com.umityasincoban.insightflow.outbox.application.OutboxEventMessage;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class AutomationWebhookTemplateResolverTest {
	
	private final AutomationWebhookTemplateResolver resolver = new AutomationWebhookTemplateResolver();
	private final OutboxEventMessage message = new OutboxEventMessage(
			UUID.randomUUID().toString(),
			UUID.randomUUID().toString(),
			"FEEDBACK",
			UUID.randomUUID().toString(),
			"feedback.ai-analysis-completed",
			1,
			Map.of(
					"sentiment", "NEGATIVE",
					"riskLevel", "CHURN_RISK",
					"priority", 10,
					"analysis", Map.of("sentiment", "NEGATIVE")
			),
			OffsetDateTime.now().toString()
	);
	
	@Test
	void resolvesEventFieldPlaceholder() {
		assertEquals(message.eventId(), resolver.resolve("{{event.eventId}}", message));
	}
	
	@Test
	void resolvesPayloadFieldPlaceholder() {
		assertEquals("CHURN_RISK", resolver.resolve("{{payload.riskLevel}}", message));
	}
	
	@Test
	void resolvesNestedPayloadPlaceholder() {
		assertEquals("NEGATIVE", resolver.resolve("{{payload.analysis.sentiment}}", message));
	}
	
	@Test
	void resolvesEmbeddedStringPlaceholder() {
		assertEquals("Risk level is CHURN_RISK", resolver.resolve("Risk level is {{payload.riskLevel}}", message));
	}
	
	@Test
	@SuppressWarnings("unchecked")
	void resolvesRecursiveMapAndListValues() {
		Object resolved = resolver.resolve(
				Map.of("items", List.of("{{payload.sentiment}}", Map.of("priority", "{{payload.priority}}"))),
				message
		);
		
		Map<String, Object> map = (Map<String, Object>) resolved;
		List<Object> items = (List<Object>) map.get("items");
		assertEquals("NEGATIVE", items.get(0));
		assertEquals(10, ((Map<String, Object>) items.get(1)).get("priority"));
	}
	
	@Test
	void missingWholeValuePlaceholderResolvesToNull() {
		assertNull(resolver.resolve("{{payload.missing}}", message));
	}
	
	@Test
	void missingEmbeddedPlaceholderResolvesToEmptyString() {
		assertEquals("Missing: ", resolver.resolve("Missing: {{payload.missing}}", message));
	}
}
