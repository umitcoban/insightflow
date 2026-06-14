package com.umityasincoban.insightflow.automation.application;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AutomationWebhookUrlValidatorTest {
	
	private final AutomationWebhookUrlValidator validator = new AutomationWebhookUrlValidator();
	
	@Test
	void acceptsValidPublicHttpsUrl() {
		assertDoesNotThrow(() -> validator.validate("https://example.com/webhooks/feedback"));
	}
	
	@Test
	void rejectsInvalidScheme() {
		assertThrows(UnsafeAutomationWebhookUrlException.class, () -> validator.validate("ftp://example.com/webhook"));
	}
	
	@Test
	void rejectsLocalhost() {
		assertThrows(UnsafeAutomationWebhookUrlException.class, () -> validator.validate("https://localhost/webhook"));
	}
	
	@Test
	void rejectsLoopbackIp() {
		assertThrows(UnsafeAutomationWebhookUrlException.class, () -> validator.validate("http://127.0.0.1/webhook"));
	}
	
	@Test
	void rejectsPrivateIp() {
		assertThrows(UnsafeAutomationWebhookUrlException.class, () -> validator.validate("http://192.168.1.10/webhook"));
	}
	
	@Test
	void rejectsEmbeddedCredentials() {
		assertThrows(UnsafeAutomationWebhookUrlException.class, () -> validator.validate("https://user:secret@example.com/webhook"));
	}
	
	@Test
	void rejectsMalformedUrl() {
		assertThrows(RuntimeException.class, () -> validator.validate("not a url"));
	}
}
