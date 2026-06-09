package com.umityasincoban.insightflow.automation.domain;

import com.umityasincoban.insightflow.tenancy.domain.TenantId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

public interface AutomationRuleRepository {
	
	AutomationRule saveNew(
			TenantId tenantId,
			String name,
			String description,
			String triggerEventType,
			Map<String, Object> conditionJson,
			List<Map<String, Object>> actionJson,
			int priority
	);
	
	Page<AutomationRule> findByTenantId(TenantId tenantId, Pageable pageable);
	
	List<AutomationRule> findActiveByTenantIdAndTriggerEventType(TenantId tenantId, String triggerEventType);
}
