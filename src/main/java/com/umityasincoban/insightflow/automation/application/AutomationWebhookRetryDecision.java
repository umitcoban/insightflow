package com.umityasincoban.insightflow.automation.application;

import java.time.OffsetDateTime;

public record AutomationWebhookRetryDecision(
		boolean retryable,
		boolean retriesExhausted,
		OffsetDateTime nextRetryAt
) {
}
