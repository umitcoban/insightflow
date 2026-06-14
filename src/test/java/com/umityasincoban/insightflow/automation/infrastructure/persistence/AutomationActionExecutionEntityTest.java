package com.umityasincoban.insightflow.automation.infrastructure.persistence;

import com.umityasincoban.insightflow.automation.domain.AutomationExecutionStatus;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class AutomationActionExecutionEntityTest {
	
	@Test
	void transitionsThroughRetryLifecycle() {
		AutomationActionExecutionEntity entity = AutomationActionExecutionEntity.pending(
				UUID.randomUUID(),
				UUID.randomUUID(),
				"WEBHOOK",
				Map.of("retryEligible", true),
				3
		);
		
		assertEquals(AutomationExecutionStatus.PENDING, entity.getStatus());
		assertEquals(0, entity.getAttemptCount());
		
		entity.markInProgress(OffsetDateTime.now());
		assertEquals(AutomationExecutionStatus.IN_PROGRESS, entity.getStatus());
		assertEquals(1, entity.getAttemptCount());
		assertNotNull(entity.getLastAttemptAt());
		
		OffsetDateTime nextRetryAt = OffsetDateTime.now().plusSeconds(5);
		entity.markRetryScheduled(Map.of("retryable", true), "timeout", nextRetryAt);
		assertEquals(AutomationExecutionStatus.RETRY_SCHEDULED, entity.getStatus());
		assertEquals(nextRetryAt, entity.getNextRetryAt());
		
		entity.markInProgress(OffsetDateTime.now());
		entity.markFailed(Map.of("retriesExhausted", true), "timeout", OffsetDateTime.now());
		assertEquals(AutomationExecutionStatus.FAILED, entity.getStatus());
		assertEquals(2, entity.getAttemptCount());
		assertNotNull(entity.getCompletedAt());
	}
	
	@Test
	void successCompletesAndClearsRetrySchedule() {
		AutomationActionExecutionEntity entity = AutomationActionExecutionEntity.pending(
				UUID.randomUUID(),
				UUID.randomUUID(),
				"WEBHOOK",
				Map.of("retryEligible", true),
				3
		);
		
		entity.markInProgress(OffsetDateTime.now());
		entity.markSuccess(Map.of("success", true), OffsetDateTime.now());
		
		assertEquals(AutomationExecutionStatus.SUCCESS, entity.getStatus());
		assertEquals(1, entity.getAttemptCount());
		assertEquals(null, entity.getNextRetryAt());
		assertNotNull(entity.getCompletedAt());
	}
}
