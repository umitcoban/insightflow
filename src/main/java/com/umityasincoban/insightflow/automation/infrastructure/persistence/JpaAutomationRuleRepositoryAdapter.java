package com.umityasincoban.insightflow.automation.infrastructure.persistence;

import com.umityasincoban.insightflow.automation.domain.AutomationRule;
import com.umityasincoban.insightflow.automation.domain.AutomationRuleId;
import com.umityasincoban.insightflow.automation.domain.AutomationRuleRepository;
import com.umityasincoban.insightflow.automation.domain.AutomationRuleStatus;
import com.umityasincoban.insightflow.tenancy.domain.TenantId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class JpaAutomationRuleRepositoryAdapter implements AutomationRuleRepository {
	
	private final AutomationRuleJpaRepository automationRuleJpaRepository;
	private final AutomationRulePersistenceMapper automationRulePersistenceMapper;
	
	public JpaAutomationRuleRepositoryAdapter(
			AutomationRuleJpaRepository automationRuleJpaRepository,
			AutomationRulePersistenceMapper automationRulePersistenceMapper
	) {
		this.automationRuleJpaRepository = automationRuleJpaRepository;
		this.automationRulePersistenceMapper = automationRulePersistenceMapper;
	}
	
	@Override
	public AutomationRule saveNew(
			TenantId tenantId,
			String name,
			String description,
			String triggerEventType,
			Map<String, Object> conditionJson,
			List<Map<String, Object>> actionJson,
			int priority
	) {
		AutomationRuleEntity entity = new AutomationRuleEntity(
				tenantId.value(),
				name,
				description,
				triggerEventType,
				conditionJson,
				actionJson,
				priority
		);
		
		AutomationRuleEntity savedEntity = automationRuleJpaRepository.save(entity);
		
		return automationRulePersistenceMapper.toDomain(savedEntity);
	}
	
	@Override
	public Page<AutomationRule> findByTenantId(TenantId tenantId, Pageable pageable) {
		return automationRuleJpaRepository.findByTenantId(tenantId.value(), pageable)
				.map(automationRulePersistenceMapper::toDomain);
	}
	
	@Override
	public Optional<AutomationRule> findByTenantIdAndId(TenantId tenantId, AutomationRuleId ruleId) {
		return automationRuleJpaRepository.findByTenantIdAndId(tenantId.value(), ruleId.value())
				.map(automationRulePersistenceMapper::toDomain);
	}
	
	@Override
	public List<AutomationRule> findActiveByTenantIdAndTriggerEventType(
			TenantId tenantId,
			String triggerEventType
	) {
		return automationRuleJpaRepository.findByTenantIdAndTriggerEventTypeAndStatusOrderByPriorityDescCreatedAtDesc(
						tenantId.value(),
						triggerEventType,
						AutomationRuleStatus.ACTIVE
				)
				.stream()
				.map(automationRulePersistenceMapper::toDomain)
				.toList();
	}
	
	@Override
	public AutomationRule save(AutomationRule automationRule) {
		AutomationRuleEntity entity = automationRuleJpaRepository.findByTenantIdAndId(
						automationRule.getTenantId().value(),
						automationRule.getId().value()
				)
				.orElseThrow(() -> new IllegalStateException("Automation rule not found: " + automationRule.getId().value()));
		
		entity.updateDetails(
				automationRule.getName(),
				automationRule.getDescription(),
				automationRule.getTriggerEventType(),
				automationRule.getConditionJson(),
				automationRule.getActionJson(),
				automationRule.getPriority()
		);
		
		if (automationRule.isActive()) {
			entity.activate();
		} else {
			entity.deactivate();
		}
		
		return automationRulePersistenceMapper.toDomain(entity);
	}
	
	@Override
	public void deleteByTenantIdAndId(TenantId tenantId, AutomationRuleId ruleId) {
		automationRuleJpaRepository.deleteByTenantIdAndId(tenantId.value(), ruleId.value());
	}
}
