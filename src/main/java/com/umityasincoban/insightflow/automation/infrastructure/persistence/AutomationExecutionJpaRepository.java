package com.umityasincoban.insightflow.automation.infrastructure.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AutomationExecutionJpaRepository extends JpaRepository<AutomationExecutionEntity, UUID> {
	
	Optional<AutomationExecutionEntity> findByTenantIdAndId(UUID tenantId, UUID id);
	
	Page<AutomationExecutionEntity> findByTenantId(UUID tenantId, Pageable pageable);
}
