package com.umityasincoban.insightflow.automation.infrastructure.persistence;

import com.umityasincoban.insightflow.automation.domain.AutomationRuleStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AutomationRuleJpaRepository extends JpaRepository<AutomationRuleEntity, UUID> {
	
	Page<AutomationRuleEntity> findByTenantId(UUID tenantId, Pageable pageable);
	
	List<AutomationRuleEntity> findByTenantIdAndTriggerEventTypeAndStatusOrderByPriorityDescCreatedAtDesc(
			UUID tenantId,
			String triggerEventType,
			AutomationRuleStatus status
	);
}
