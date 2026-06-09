package com.umityasincoban.insightflow.automation.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AutomationActionExecutionJpaRepository extends JpaRepository<AutomationActionExecutionEntity, UUID> {
	
	List<AutomationActionExecutionEntity> findByTenantIdAndExecutionIdOrderByCreatedAtAsc(UUID tenantId, UUID executionId);
}
