package com.umityasincoban.insightflow.automation.application;

import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.Map;
import java.util.Set;

@Component
public class AutomationWebhookRetryPolicy {
	
	private static final Set<Integer> RETRYABLE_HTTP_STATUSES = Set.of(408, 425, 429, 500, 502, 503, 504);
	private static final Set<String> RETRYABLE_ERROR_TYPES = Set.of("TIMEOUT", "NETWORK_ERROR", "DNS_RESOLUTION_FAILED");
	private static final Set<String> NON_RETRYABLE_ERROR_TYPES = Set.of(
			"CONFIGURATION_ERROR",
			"UNSAFE_URL",
			"INVALID_URL",
			"UNSUPPORTED_METHOD",
			"SENSITIVE_HEADERS_NOT_PERSISTED"
	);
	
	private final AutomationWebhookProperties properties;
	
	public AutomationWebhookRetryPolicy(AutomationWebhookProperties properties) {
		this.properties = properties;
	}
	
	public AutomationWebhookRetryDecision decide(
			Map<String, Object> resultPayload,
			int attempt,
			int maxAttempts,
			OffsetDateTime now
	) {
		if (!properties.getRetry().isEnabled() || attempt >= maxAttempts || !isRetryable(resultPayload)) {
			return new AutomationWebhookRetryDecision(false, attempt >= maxAttempts, null);
		}
		
		return new AutomationWebhookRetryDecision(true, false, now.plus(delay(resultPayload, attempt)));
	}
	
	public boolean isRetryable(Map<String, Object> resultPayload) {
		Object errorType = resultPayload.get("errorType");
		if (errorType != null) {
			String value = errorType.toString();
			if (NON_RETRYABLE_ERROR_TYPES.contains(value)) {
				return false;
			}
			return RETRYABLE_ERROR_TYPES.contains(value);
		}
		
		Object httpStatus = resultPayload.get("httpStatus");
		if (httpStatus instanceof Number number) {
			return RETRYABLE_HTTP_STATUSES.contains(number.intValue());
		}
		
		return false;
	}
	
	private Duration delay(Map<String, Object> resultPayload, int attempt) {
		Duration retryAfter = retryAfter(resultPayload);
		if (retryAfter != null) {
			return cap(retryAfter);
		}
		
		double multiplier = Math.pow(properties.getRetry().getMultiplier(), Math.max(0, attempt - 1));
		long delayMs = (long) (properties.getRetry().getInitialDelayMs() * multiplier);
		return cap(Duration.ofMillis(delayMs));
	}
	
	private Duration cap(Duration duration) {
		Duration maxDelay = Duration.ofMillis(properties.getRetry().getMaxDelayMs());
		return duration.compareTo(maxDelay) > 0 ? maxDelay : duration;
	}
	
	private static Duration retryAfter(Map<String, Object> resultPayload) {
		Object value = resultPayload.get("retryAfter");
		if (value == null || value.toString().isBlank()) {
			return null;
		}
		
		String text = value.toString().trim();
		try {
			return Duration.ofSeconds(Long.parseLong(text));
		} catch (NumberFormatException exception) {
			try {
				OffsetDateTime retryAt = OffsetDateTime.parse(text);
				return Duration.between(OffsetDateTime.now(), retryAt).isNegative()
						? Duration.ZERO
						: Duration.between(OffsetDateTime.now(), retryAt);
			} catch (DateTimeParseException ignored) {
				return null;
			}
		}
	}
}
