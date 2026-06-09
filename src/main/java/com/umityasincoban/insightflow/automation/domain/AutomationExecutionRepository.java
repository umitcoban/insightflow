package com.umityasincoban.insightflow.automation.domain;

import com.umityasincoban.insightflow.tenancy.domain.TenantId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface AutomationExecutionRepository {
	
	AutomationExecution startExecution(
			TenantId tenantId,
			AutomationRuleId ruleId,
			UUID sourceEventId,
			String sourceEventType
	);
	
	AutomationExecution markSkipped(TenantId tenantId, AutomationExecutionId executionId);
	
	AutomationExecution markSuccess(TenantId tenantId, AutomationExecutionId executionId, boolean matched);
	
	AutomationExecution markFailed(
			TenantId tenantId,
			AutomationExecutionId executionId,
			boolean matched,
			String errorMessage
	);
	
	Page<AutomationExecution> findByTenantId(TenantId tenantId, Pageable pageable);
}
