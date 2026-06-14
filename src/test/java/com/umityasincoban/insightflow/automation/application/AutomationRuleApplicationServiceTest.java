package com.umityasincoban.insightflow.automation.application;

import com.umityasincoban.insightflow.automation.domain.AutomationRule;
import com.umityasincoban.insightflow.automation.domain.AutomationRuleId;
import com.umityasincoban.insightflow.automation.domain.AutomationRuleRepository;
import com.umityasincoban.insightflow.automation.domain.AutomationRuleStatus;
import com.umityasincoban.insightflow.shared.tenancy.CurrentTenantProvider;
import com.umityasincoban.insightflow.tenancy.domain.TenantId;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AutomationRuleApplicationServiceTest {
	
	private final AutomationRuleRepository automationRuleRepository = mock(AutomationRuleRepository.class);
	private final CurrentTenantProvider currentTenantProvider = mock(CurrentTenantProvider.class);
	private final AutomationRuleApplicationService service = new AutomationRuleApplicationService(
			automationRuleRepository,
			currentTenantProvider
	);
	
	@Test
	void updateRuleChangesOnlyProvidedFields() {
		TenantId tenantId = TenantId.of(UUID.randomUUID());
		UUID ruleId = UUID.randomUUID();
		AutomationRule existingRule = rule(tenantId, ruleId, AutomationRuleStatus.ACTIVE);
		
		when(currentTenantProvider.getCurrentTenantId()).thenReturn(tenantId);
		when(automationRuleRepository.findByTenantIdAndId(tenantId, AutomationRuleId.of(ruleId)))
				.thenReturn(Optional.of(existingRule));
		when(automationRuleRepository.save(any(AutomationRule.class)))
				.thenAnswer(invocation -> invocation.getArgument(0));
		
		AutomationRule updatedRule = service.updateRule(
				ruleId,
				"Updated rule",
				null,
				null,
				null,
				null,
				200
		);
		
		assertThat(updatedRule.getName()).isEqualTo("Updated rule");
		assertThat(updatedRule.getDescription()).isEqualTo("Original description");
		assertThat(updatedRule.getTriggerEventType()).isEqualTo("feedback.ai-analysis-completed");
		assertThat(updatedRule.getConditionJson()).containsEntry("sentiment", "NEGATIVE");
		assertThat(updatedRule.getActionJson()).hasSize(1);
		assertThat(updatedRule.getPriority()).isEqualTo(200);
		assertThat(updatedRule.getStatus()).isEqualTo(AutomationRuleStatus.ACTIVE);
	}
	
	@Test
	void deactivateRuleChangesStatusToInactive() {
		TenantId tenantId = TenantId.of(UUID.randomUUID());
		UUID ruleId = UUID.randomUUID();
		AutomationRule existingRule = rule(tenantId, ruleId, AutomationRuleStatus.ACTIVE);
		
		when(currentTenantProvider.getCurrentTenantId()).thenReturn(tenantId);
		when(automationRuleRepository.findByTenantIdAndId(tenantId, AutomationRuleId.of(ruleId)))
				.thenReturn(Optional.of(existingRule));
		when(automationRuleRepository.save(any(AutomationRule.class)))
				.thenAnswer(invocation -> invocation.getArgument(0));
		
		AutomationRule deactivatedRule = service.deactivateRule(ruleId);
		
		assertThat(deactivatedRule.getStatus()).isEqualTo(AutomationRuleStatus.INACTIVE);
	}
	
	@Test
	void activateRuleChangesStatusToActive() {
		TenantId tenantId = TenantId.of(UUID.randomUUID());
		UUID ruleId = UUID.randomUUID();
		AutomationRule existingRule = rule(tenantId, ruleId, AutomationRuleStatus.INACTIVE);
		
		when(currentTenantProvider.getCurrentTenantId()).thenReturn(tenantId);
		when(automationRuleRepository.findByTenantIdAndId(tenantId, AutomationRuleId.of(ruleId)))
				.thenReturn(Optional.of(existingRule));
		when(automationRuleRepository.save(any(AutomationRule.class)))
				.thenAnswer(invocation -> invocation.getArgument(0));
		
		AutomationRule activatedRule = service.activateRule(ruleId);
		
		assertThat(activatedRule.getStatus()).isEqualTo(AutomationRuleStatus.ACTIVE);
	}
	
	private static AutomationRule rule(TenantId tenantId, UUID ruleId, AutomationRuleStatus status) {
		return new AutomationRule(
				AutomationRuleId.of(ruleId),
				tenantId,
				"Original rule",
				"Original description",
				"feedback.ai-analysis-completed",
				Map.of("sentiment", "NEGATIVE"),
				List.of(Map.of("type", "LOG", "message", "Log it")),
				status,
				100,
				OffsetDateTime.now().minusDays(1),
				OffsetDateTime.now().minusDays(1)
		);
	}
}
