package com.umityasincoban.insightflow.outbox.application;

import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.OffsetDateTime;

@Component
public class OutboxRetryPolicy {
	
	private static final int MAX_RETRY_COUNT = 3;
	private static final long BASE_DELAY_SECONDS = 10;
	
	public boolean shouldRetry(int currentRetryCount) {
		return currentRetryCount < MAX_RETRY_COUNT;
	}
	
	public OffsetDateTime nextRetryAt(int currentRetryCount) {
		long delaySeconds = BASE_DELAY_SECONDS * (long) Math.pow(2, currentRetryCount);
		
		return OffsetDateTime.now().plus(Duration.ofSeconds(delaySeconds));
	}
}