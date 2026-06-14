package com.umityasincoban.insightflow.automation.application;

import com.umityasincoban.insightflow.automation.domain.AutomationActionExecutionRepository;
import com.umityasincoban.insightflow.automation.domain.AutomationExecutionId;
import com.umityasincoban.insightflow.automation.domain.AutomationExecutionRepository;
import com.umityasincoban.insightflow.shared.tenancy.CurrentTenantProvider;
import com.umityasincoban.insightflow.tenancy.domain.TenantId;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AutomationExecutionApplicationServiceTest {
	
	private final AutomationExecutionRepository automationExecutionRepository = mock(AutomationExecutionRepository.class);
	private final AutomationActionExecutionRepository automationActionExecutionRepository = mock(
			AutomationActionExecutionRepository.class
	);
	private final CurrentTenantProvider currentTenantProvider = mock(CurrentTenantProvider.class);
	private final AutomationExecutionApplicationService service = new AutomationExecutionApplicationService(
			automationExecutionRepository,
			automationActionExecutionRepository,
			currentTenantProvider
	);
	
	@Test
	void listActionExecutionsRequiresTenantScopedExecution() {
		TenantId tenantId = TenantId.of(UUID.randomUUID());
		UUID executionId = UUID.randomUUID();
		
		when(currentTenantProvider.getCurrentTenantId()).thenReturn(tenantId);
		when(automationExecutionRepository.findByTenantIdAndId(tenantId, AutomationExecutionId.of(executionId)))
				.thenReturn(Optional.empty());
		
		assertThatThrownBy(() -> service.listActionExecutions(executionId))
				.isInstanceOf(AutomationExecutionNotFoundException.class);
		
		verify(automationActionExecutionRepository, never()).findByTenantIdAndExecutionIdOrderByCreatedAtAsc(
				tenantId,
				AutomationExecutionId.of(executionId)
		);
	}
}
