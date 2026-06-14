package com.umityasincoban.insightflow.automation.application;

import com.umityasincoban.insightflow.automation.domain.AutomationExecutionRepository;
import com.umityasincoban.insightflow.automation.domain.AutomationRule;
import com.umityasincoban.insightflow.automation.domain.AutomationRuleId;
import com.umityasincoban.insightflow.automation.domain.AutomationRuleRepository;
import com.umityasincoban.insightflow.automation.domain.AutomationRuleStatus;
import com.umityasincoban.insightflow.outbox.application.OutboxEventMessage;
import com.umityasincoban.insightflow.tenancy.domain.TenantId;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AutomationRuleEvaluationServiceTest {
	
	private final AutomationRuleRepository automationRuleRepository = mock(AutomationRuleRepository.class);
	private final AutomationExecutionRepository automationExecutionRepository = mock(AutomationExecutionRepository.class);
	private final AutomationActionExecutionService automationActionExecutionService = mock(AutomationActionExecutionService.class);
	private final AutomationRuleEvaluationService service = new AutomationRuleEvaluationService(
			automationRuleRepository,
			automationExecutionRepository,
			new AutomationConditionEvaluator(),
			automationActionExecutionService
	);
	
	@Test
	void idempotencyCheckPreventsDuplicateExecutionForSameRuleAndSourceEvent() {
		TenantId tenantId = TenantId.of(UUID.randomUUID());
		UUID sourceEventId = UUID.randomUUID();
		AutomationRule rule = rule(tenantId);
		OutboxEventMessage message = message(tenantId, sourceEventId);
		
		when(automationRuleRepository.findActiveByTenantIdAndTriggerEventType(
				tenantId,
				"feedback.ai-analysis-completed"
		)).thenReturn(List.of(rule));
		when(automationExecutionRepository.existsByTenantIdAndRuleIdAndSourceEventId(
				tenantId,
				rule.getId(),
				sourceEventId
		)).thenReturn(true);
		
		service.evaluate(message);
		
		verify(automationExecutionRepository, never()).startExecution(
				tenantId,
				rule.getId(),
				sourceEventId,
				"feedback.ai-analysis-completed"
		);
	}
	
	private static AutomationRule rule(TenantId tenantId) {
		return new AutomationRule(
				AutomationRuleId.of(UUID.randomUUID()),
				tenantId,
				"Rule",
				null,
				"feedback.ai-analysis-completed",
				Map.of(),
				List.of(Map.of("type", "LOG", "message", "Log it")),
				AutomationRuleStatus.ACTIVE,
				100,
				OffsetDateTime.now().minusDays(1),
				OffsetDateTime.now().minusDays(1)
		);
	}
	
	private static OutboxEventMessage message(TenantId tenantId, UUID sourceEventId) {
		return new OutboxEventMessage(
				sourceEventId.toString(),
				tenantId.value().toString(),
				"FEEDBACK",
				UUID.randomUUID().toString(),
				"feedback.ai-analysis-completed",
				1,
				Map.of("sentiment", "NEGATIVE"),
				OffsetDateTime.now().toString()
		);
	}
}
