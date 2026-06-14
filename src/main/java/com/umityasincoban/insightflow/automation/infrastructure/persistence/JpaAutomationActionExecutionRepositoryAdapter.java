package com.umityasincoban.insightflow.automation.infrastructure.persistence;

import com.umityasincoban.insightflow.automation.domain.AutomationActionExecution;
import com.umityasincoban.insightflow.automation.domain.AutomationActionExecutionRepository;
import com.umityasincoban.insightflow.automation.domain.AutomationExecutionId;
import com.umityasincoban.insightflow.tenancy.domain.TenantId;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

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
	public AutomationActionExecution savePending(
			TenantId tenantId,
			AutomationExecutionId executionId,
			String actionType,
			Map<String, Object> requestPayload,
			int maxAttempts
	) {
		AutomationActionExecutionEntity entity = AutomationActionExecutionEntity.pending(
				tenantId.value(),
				executionId.value(),
				actionType,
				requestPayload,
				maxAttempts
		);
		
		return automationActionExecutionPersistenceMapper.toDomain(
				automationActionExecutionJpaRepository.save(entity)
		);
	}
	
	@Override
	@Transactional
	public AutomationActionExecution markInProgress(
			TenantId tenantId,
			com.umityasincoban.insightflow.automation.domain.AutomationActionExecutionId actionExecutionId,
			OffsetDateTime lastAttemptAt
	) {
		AutomationActionExecutionEntity entity = getTenantScopedEntity(tenantId, actionExecutionId);
		entity.markInProgress(lastAttemptAt);
		return automationActionExecutionPersistenceMapper.toDomain(entity);
	}
	
	@Override
	@Transactional
	public AutomationActionExecution markSuccess(
			TenantId tenantId,
			com.umityasincoban.insightflow.automation.domain.AutomationActionExecutionId actionExecutionId,
			Map<String, Object> resultPayload,
			OffsetDateTime completedAt
	) {
		AutomationActionExecutionEntity entity = getTenantScopedEntity(tenantId, actionExecutionId);
		entity.markSuccess(resultPayload, completedAt);
		return automationActionExecutionPersistenceMapper.toDomain(entity);
	}
	
	@Override
	@Transactional
	public AutomationActionExecution markRetryScheduled(
			TenantId tenantId,
			com.umityasincoban.insightflow.automation.domain.AutomationActionExecutionId actionExecutionId,
			Map<String, Object> resultPayload,
			String errorMessage,
			OffsetDateTime nextRetryAt
	) {
		AutomationActionExecutionEntity entity = getTenantScopedEntity(tenantId, actionExecutionId);
		entity.markRetryScheduled(resultPayload, errorMessage, nextRetryAt);
		return automationActionExecutionPersistenceMapper.toDomain(entity);
	}
	
	@Override
	@Transactional
	public AutomationActionExecution markFailed(
			TenantId tenantId,
			com.umityasincoban.insightflow.automation.domain.AutomationActionExecutionId actionExecutionId,
			Map<String, Object> resultPayload,
			String errorMessage,
			OffsetDateTime completedAt
	) {
		AutomationActionExecutionEntity entity = getTenantScopedEntity(tenantId, actionExecutionId);
		entity.markFailed(resultPayload, errorMessage, completedAt);
		return automationActionExecutionPersistenceMapper.toDomain(entity);
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
			Map<String, Object> resultPayload,
			String errorMessage
	) {
		AutomationActionExecutionEntity entity = AutomationActionExecutionEntity.failed(
				tenantId.value(),
				executionId.value(),
				actionType,
				requestPayload,
				resultPayload,
				errorMessage
		);
		
		return automationActionExecutionPersistenceMapper.toDomain(
				automationActionExecutionJpaRepository.save(entity)
		);
	}
	
	@Override
	public List<AutomationActionExecution> findByTenantIdAndExecutionIdOrderByCreatedAtAsc(
			TenantId tenantId,
			AutomationExecutionId executionId
	) {
		return automationActionExecutionJpaRepository.findByTenantIdAndExecutionIdOrderByCreatedAtAsc(
						tenantId.value(),
						executionId.value()
				)
				.stream()
				.map(automationActionExecutionPersistenceMapper::toDomain)
				.toList();
	}
	
	@Override
	public Optional<AutomationActionExecution> findByTenantIdAndId(
			TenantId tenantId,
			com.umityasincoban.insightflow.automation.domain.AutomationActionExecutionId actionExecutionId
	) {
		return automationActionExecutionJpaRepository.findByTenantIdAndId(tenantId.value(), actionExecutionId.value())
				.map(automationActionExecutionPersistenceMapper::toDomain);
	}
	
	@Override
	@Transactional
	public List<AutomationActionExecution> claimDueWebhookRetries(
			int limit,
			OffsetDateTime now,
			OffsetDateTime staleBefore
	) {
		List<AutomationActionExecutionEntity> entities = automationActionExecutionJpaRepository.findDueWebhookRetriesForUpdate(
				limit,
				now,
				staleBefore
		);
		for (AutomationActionExecutionEntity entity : entities) {
			entity.markInProgress(now);
		}
		
		return entities.stream()
				.map(automationActionExecutionPersistenceMapper::toDomain)
				.toList();
	}
	
	@Override
	public List<AutomationActionExecution> findByExecutionId(UUID executionId) {
		return automationActionExecutionJpaRepository.findByExecutionId(executionId)
				.stream()
				.map(automationActionExecutionPersistenceMapper::toDomain)
				.toList();
	}
	
	private AutomationActionExecutionEntity getTenantScopedEntity(
			TenantId tenantId,
			com.umityasincoban.insightflow.automation.domain.AutomationActionExecutionId actionExecutionId
	) {
		return automationActionExecutionJpaRepository.findByTenantIdAndId(tenantId.value(), actionExecutionId.value())
				.orElseThrow(() -> new IllegalStateException("Automation action execution not found: " + actionExecutionId.value()));
	}
}
