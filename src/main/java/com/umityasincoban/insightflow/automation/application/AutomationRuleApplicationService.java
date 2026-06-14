package com.umityasincoban.insightflow.automation.application;

import com.umityasincoban.insightflow.automation.domain.AutomationRule;
import com.umityasincoban.insightflow.automation.domain.AutomationRuleId;
import com.umityasincoban.insightflow.automation.domain.AutomationRuleRepository;
import com.umityasincoban.insightflow.shared.tenancy.CurrentTenantProvider;
import com.umityasincoban.insightflow.tenancy.domain.TenantId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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
	
	@Transactional(readOnly = true)
	public AutomationRule getRule(UUID ruleId) {
		TenantId tenantId = currentTenantProvider.getCurrentTenantId();
		
		return automationRuleRepository.findByTenantIdAndId(tenantId, AutomationRuleId.of(ruleId))
				.orElseThrow(() -> new AutomationRuleNotFoundException(ruleId));
	}
	
	@Transactional
	public AutomationRule updateRule(
			UUID ruleId,
			String name,
			String description,
			String triggerEventType,
			Map<String, Object> conditionJson,
			List<Map<String, Object>> actionJson,
			Integer priority
	) {
		validatePartialUpdate(name, triggerEventType, actionJson);
		
		TenantId tenantId = currentTenantProvider.getCurrentTenantId();
		AutomationRule existingRule = automationRuleRepository.findByTenantIdAndId(tenantId, AutomationRuleId.of(ruleId))
				.orElseThrow(() -> new AutomationRuleNotFoundException(ruleId));
		
		return automationRuleRepository.save(existingRule.updateDetails(
				name,
				description,
				triggerEventType,
				conditionJson,
				actionJson,
				priority,
				OffsetDateTime.now()
		));
	}
	
	@Transactional
	public AutomationRule activateRule(UUID ruleId) {
		TenantId tenantId = currentTenantProvider.getCurrentTenantId();
		AutomationRule rule = automationRuleRepository.findByTenantIdAndId(tenantId, AutomationRuleId.of(ruleId))
				.orElseThrow(() -> new AutomationRuleNotFoundException(ruleId));
		
		rule.activate();
		
		return automationRuleRepository.save(rule);
	}
	
	@Transactional
	public AutomationRule deactivateRule(UUID ruleId) {
		TenantId tenantId = currentTenantProvider.getCurrentTenantId();
		AutomationRule rule = automationRuleRepository.findByTenantIdAndId(tenantId, AutomationRuleId.of(ruleId))
				.orElseThrow(() -> new AutomationRuleNotFoundException(ruleId));
		
		rule.deactivate();
		
		return automationRuleRepository.save(rule);
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
	
	private static void validatePartialUpdate(
			String name,
			String triggerEventType,
			List<Map<String, Object>> actionJson
	) {
		if (name != null && name.isBlank()) {
			throw new IllegalArgumentException("Automation rule name cannot be blank");
		}
		
		if (triggerEventType != null && triggerEventType.isBlank()) {
			throw new IllegalArgumentException("Automation rule trigger event type cannot be blank");
		}
		
		if (actionJson != null && actionJson.isEmpty()) {
			throw new IllegalArgumentException("Automation rule actions cannot be empty");
		}
	}
}
