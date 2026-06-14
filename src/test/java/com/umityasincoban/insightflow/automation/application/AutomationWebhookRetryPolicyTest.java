package com.umityasincoban.insightflow.automation.application;

import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AutomationWebhookRetryPolicyTest {
	
	private final AutomationWebhookProperties properties = properties();
	private final AutomationWebhookRetryPolicy policy = new AutomationWebhookRetryPolicy(properties);
	
	@Test
	void timeoutIsRetryable() {
		assertTrue(policy.isRetryable(Map.of("errorType", "TIMEOUT")));
	}
	
	@Test
	void http429IsRetryable() {
		assertTrue(policy.isRetryable(Map.of("httpStatus", 429)));
	}
	
	@Test
	void http503IsRetryable() {
		assertTrue(policy.isRetryable(Map.of("httpStatus", 503)));
	}
	
	@Test
	void http400IsNotRetryable() {
		assertFalse(policy.isRetryable(Map.of("httpStatus", 400)));
	}
	
	@Test
	void unsafeUrlIsNotRetryable() {
		assertFalse(policy.isRetryable(Map.of("errorType", "UNSAFE_URL")));
	}
	
	@Test
	void attemptsExhaustedPreventsRetry() {
		AutomationWebhookRetryDecision decision = policy.decide(Map.of("errorType", "TIMEOUT"), 3, 3, OffsetDateTime.now());
		
		assertFalse(decision.retryable());
		assertTrue(decision.retriesExhausted());
	}
	
	@Test
	void exponentialBackoffIsCapped() {
		OffsetDateTime now = OffsetDateTime.now();
		AutomationWebhookRetryDecision decision = policy.decide(Map.of("errorType", "TIMEOUT"), 5, 10, now);
		
		assertTrue(decision.nextRetryAt().isBefore(now.plusSeconds(3)));
	}
	
	@Test
	void retryAfterIsRespectedAndCapped() {
		OffsetDateTime now = OffsetDateTime.now();
		AutomationWebhookRetryDecision decision = policy.decide(Map.of("httpStatus", 503, "retryAfter", "30"), 1, 3, now);
		
		assertTrue(decision.nextRetryAt().isBefore(now.plusSeconds(3)));
	}
	
	private static AutomationWebhookProperties properties() {
		AutomationWebhookProperties properties = new AutomationWebhookProperties();
		properties.getRetry().setInitialDelayMs(1000);
		properties.getRetry().setMultiplier(2.0);
		properties.getRetry().setMaxDelayMs(2000);
		properties.getRetry().setMaxAttempts(3);
		return properties;
	}
}
