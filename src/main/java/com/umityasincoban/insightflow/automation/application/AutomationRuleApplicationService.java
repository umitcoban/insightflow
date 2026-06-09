package com.umityasincoban.insightflow.automation.application;

import com.umityasincoban.insightflow.automation.domain.AutomationRule;
import com.umityasincoban.insightflow.automation.domain.AutomationRuleRepository;
import com.umityasincoban.insightflow.shared.tenancy.CurrentTenantProvider;
import com.umityasincoban.insightflow.tenancy.domain.TenantId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class AutomationRuleApplicationService {
	
	private final AutomationRuleRepository automationRuleRepository;
	private final CurrentTenantProvider currentTenantProvider;
	
	public AutomationRuleApplicationService(
			AutomationRuleRepository automationRuleRepository,
			CurrentTenantProvider currentTenantProvider
	) {
		this.automationRuleRepository = automationRuleRepository;
		this.currentTenantProvider = currentTenantProvider;
	}
	
	@Transactional
	public AutomationRule createRule(
			String name,
			String description,
			String triggerEventType,
			Map<String, Object> conditionJson,
			List<Map<String, Object>> actionJson,
			Integer priority
	) {
		TenantId tenantId = currentTenantProvider.getCurrentTenantId();
		
		return automationRuleRepository.saveNew(
				tenantId,
				name,
				description,
				triggerEventType,
				conditionJson,
				actionJson,
				priority == null ? 0 : priority
		);
	}
	
	@Transactional(readOnly = true)
	public Page<AutomationRule> listRules(Integer page, Integer size) {
		TenantId tenantId = currentTenantProvider.getCurrentTenantId();
		
		PageRequest pageRequest = PageRequest.of(
				normalizePage(page),
				normalizeSize(size),
				Sort.by(
						Sort.Order.desc("priority"),
						Sort.Order.desc("createdAt")
				)
		);
		
		return automationRuleRepository.findByTenantId(tenantId, pageRequest);
	}
	
	private static int normalizePage(Integer page) {
		if (page == null || page < 0) {
			return 0;
		}
		
		return page;
	}
	
	private static int normalizeSize(Integer size) {
		if (size == null || size < 1) {
			return 20;
		}
		
		return Math.min(size, 100);
	}
}
