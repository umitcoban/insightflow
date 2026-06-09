package com.umityasincoban.insightflow.automation.infrastructure.persistence;

import com.umityasincoban.insightflow.automation.domain.AutomationActionExecution;
import com.umityasincoban.insightflow.automation.domain.AutomationActionExecutionRepository;
import com.umityasincoban.insightflow.automation.domain.AutomationExecutionId;
import com.umityasincoban.insightflow.tenancy.domain.TenantId;
import org.springframework.stereotype.Repository;

import java.util.Map;

@Repository
public class JpaAutomationActionExecutionRepositoryAdapter implements AutomationActionExecutionRepository {
	
	private final AutomationActionExecutionJpaRepository automationActionExecutionJpaRepository;
	private final AutomationActionExecutionPersistenceMapper automationActionExecutionPersistenceMapper;
	
	public JpaAutomationActionExecutionRepositoryAdapter(
			AutomationActionExecutionJpaRepository automationActionExecutionJpaRepository,
			AutomationActionExecutionPersistenceMapper automationActionExecutionPersistenceMapper
	) {
		this.automationActionExecutionJpaRepository = automationActionExecutionJpaRepository;
		this.automationActionExecutionPersistenceMapper = automationActionExecutionPersistenceMapper;
	}
	
	@Override
	public AutomationActionExecution saveSuccess(
			TenantId tenantId,
			AutomationExecutionId executionId,
			String actionType,
			Map<String, Object> requestPayload,
			Map<String, Object> resultPayload
	) {
		AutomationActionExecutionEntity entity = AutomationActionExecutionEntity.success(
				tenantId.value(),
				executionId.value(),
				actionType,
				requestPayload,
				resultPayload
		);
		
		return automationActionExecutionPersistenceMapper.toDomain(
				automationActionExecutionJpaRepository.save(entity)
		);
	}
	
	@Override
	public AutomationActionExecution saveFailed(
			TenantId tenantId,
			AutomationExecutionId executionId,
			String actionType,
			Map<String, Object> requestPayload,
			String errorMessage
	) {
		AutomationActionExecutionEntity entity = AutomationActionExecutionEntity.failed(
				tenantId.value(),
				executionId.value(),
				actionType,
				requestPayload,
				errorMessage
		);
		
		return automationActionExecutionPersistenceMapper.toDomain(
				automationActionExecutionJpaRepository.save(entity)
		);
	}
}
