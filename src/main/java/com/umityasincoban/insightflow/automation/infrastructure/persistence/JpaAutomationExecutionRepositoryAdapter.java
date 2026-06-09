package com.umityasincoban.insightflow.automation.infrastructure.persistence;

import com.umityasincoban.insightflow.automation.domain.AutomationExecution;
import com.umityasincoban.insightflow.automation.domain.AutomationExecutionId;
import com.umityasincoban.insightflow.automation.domain.AutomationExecutionRepository;
import com.umityasincoban.insightflow.automation.domain.AutomationRuleId;
import com.umityasincoban.insightflow.tenancy.domain.TenantId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public class JpaAutomationExecutionRepositoryAdapter implements AutomationExecutionRepository {
	
	private final AutomationExecutionJpaRepository automationExecutionJpaRepository;
	private final AutomationExecutionPersistenceMapper automationExecutionPersistenceMapper;
	
	public JpaAutomationExecutionRepositoryAdapter(
			AutomationExecutionJpaRepository automationExecutionJpaRepository,
			AutomationExecutionPersistenceMapper automationExecutionPersistenceMapper
	) {
		this.automationExecutionJpaRepository = automationExecutionJpaRepository;
		this.automationExecutionPersistenceMapper = automationExecutionPersistenceMapper;
	}
	
	@Override
	public AutomationExecution startExecution(
			TenantId tenantId,
			AutomationRuleId ruleId,
			UUID sourceEventId,
			String sourceEventType
	) {
		AutomationExecutionEntity entity = new AutomationExecutionEntity(
				tenantId.value(),
				ruleId.value(),
				sourceEventId,
				sourceEventType
		);
		
		return automationExecutionPersistenceMapper.toDomain(automationExecutionJpaRepository.save(entity));
	}
	
	@Override
	public AutomationExecution markSkipped(TenantId tenantId, AutomationExecutionId executionId) {
		AutomationExecutionEntity entity = getTenantScopedEntity(tenantId, executionId);
		entity.markSkipped();
		
		return automationExecutionPersistenceMapper.toDomain(entity);
	}
	
	@Override
	public AutomationExecution markSuccess(TenantId tenantId, AutomationExecutionId executionId, boolean matched) {
		AutomationExecutionEntity entity = getTenantScopedEntity(tenantId, executionId);
		entity.markSuccess(matched);
		
		return automationExecutionPersistenceMapper.toDomain(entity);
	}
	
	@Override
	public AutomationExecution markFailed(
			TenantId tenantId,
			AutomationExecutionId executionId,
			boolean matched,
			String errorMessage
	) {
		AutomationExecutionEntity entity = getTenantScopedEntity(tenantId, executionId);
		entity.markFailed(matched, errorMessage);
		
		return automationExecutionPersistenceMapper.toDomain(entity);
	}
	
	@Override
	public Page<AutomationExecution> findByTenantId(TenantId tenantId, Pageable pageable) {
		return automationExecutionJpaRepository.findByTenantId(tenantId.value(), pageable)
				.map(automationExecutionPersistenceMapper::toDomain);
	}
	
	private AutomationExecutionEntity getTenantScopedEntity(TenantId tenantId, AutomationExecutionId executionId) {
		return automationExecutionJpaRepository.findByTenantIdAndId(tenantId.value(), executionId.value())
				.orElseThrow(() -> new IllegalStateException("Automation execution not found: " + executionId.value()));
	}
}
