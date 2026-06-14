package com.umityasincoban.insightflow.automation.application;

import com.umityasincoban.insightflow.automation.domain.AutomationActionExecution;
import com.umityasincoban.insightflow.automation.domain.AutomationActionExecutionId;
import com.umityasincoban.insightflow.automation.domain.AutomationActionExecutionRepository;
import com.umityasincoban.insightflow.automation.domain.AutomationExecutionId;
import com.umityasincoban.insightflow.automation.domain.AutomationExecutionRepository;
import com.umityasincoban.insightflow.automation.domain.AutomationExecutionStatus;
import com.umityasincoban.insightflow.tenancy.domain.TenantId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@Service
public class AutomationActionExecutionPersistenceService {
	
	private final AutomationActionExecutionRepository actionExecutionRepository;
	private final AutomationExecutionRepository executionRepository;
	
	public AutomationActionExecutionPersistenceService(
			AutomationActionExecutionRepository actionExecutionRepository,
			AutomationExecutionRepository executionRepository
	) {
		this.actionExecutionRepository = actionExecutionRepository;
		this.executionRepository = executionRepository;
	}
	
	@Transactional
	public AutomationActionExecution createPendingWebhookAction(
			TenantId tenantId,
			AutomationExecutionId executionId,
			Map<String, Object> requestPayload,
			int maxAttempts
	) {
		return actionExecutionRepository.savePending(tenantId, executionId, "WEBHOOK", requestPayload, maxAttempts);
	}
	
	@Transactional
	public AutomationActionExecution markAttemptStarted(
			TenantId tenantId,
			AutomationActionExecutionId actionExecutionId,
			OffsetDateTime now
	) {
		return actionExecutionRepository.markInProgress(tenantId, actionExecutionId, now);
	}
	
	@Transactional
	public AutomationActionExecution completeSuccess(
			TenantId tenantId,
			AutomationExecutionId executionId,
			AutomationActionExecutionId actionExecutionId,
			Map<String, Object> resultPayload,
			OffsetDateTime now
	) {
		AutomationActionExecution action = actionExecutionRepository.markSuccess(tenantId, actionExecutionId, resultPayload, now);
		recalculateParent(tenantId, executionId);
		return action;
	}
	
	@Transactional
	public AutomationActionExecution scheduleRetry(
			TenantId tenantId,
			AutomationExecutionId executionId,
			AutomationActionExecutionId actionExecutionId,
			Map<String, Object> resultPayload,
			String errorMessage,
			OffsetDateTime nextRetryAt
	) {
		AutomationActionExecution action = actionExecutionRepository.markRetryScheduled(
				tenantId,
				actionExecutionId,
				resultPayload,
				errorMessage,
				nextRetryAt
		);
		recalculateParent(tenantId, executionId);
		return action;
	}
	
	@Transactional
	public AutomationActionExecution completeFailure(
			TenantId tenantId,
			AutomationExecutionId executionId,
			AutomationActionExecutionId actionExecutionId,
			Map<String, Object> resultPayload,
			String errorMessage,
			OffsetDateTime now
	) {
		AutomationActionExecution action = actionExecutionRepository.markFailed(
				tenantId,
				actionExecutionId,
				resultPayload,
				errorMessage,
				now
		);
		recalculateParent(tenantId, executionId);
		return action;
	}
	
	@Transactional
	public List<AutomationActionExecution> claimDueWebhookRetries(int limit, OffsetDateTime now, OffsetDateTime staleBefore) {
		return actionExecutionRepository.claimDueWebhookRetries(limit, now, staleBefore);
	}
	
	private void recalculateParent(TenantId tenantId, AutomationExecutionId executionId) {
		List<AutomationActionExecution> actions = actionExecutionRepository.findByExecutionId(executionId.value());
		if (actions.stream().anyMatch(action -> action.getStatus() == AutomationExecutionStatus.FAILED)) {
			String errorMessage = actions.stream()
					.filter(action -> action.getStatus() == AutomationExecutionStatus.FAILED)
					.map(AutomationActionExecution::getErrorMessage)
					.filter(message -> message != null && !message.isBlank())
					.findFirst()
					.orElse("Automation action failed");
			executionRepository.markFailed(tenantId, executionId, true, errorMessage);
			return;
		}
		
		if (actions.stream().anyMatch(action -> action.getStatus() == AutomationExecutionStatus.PENDING
				|| action.getStatus() == AutomationExecutionStatus.IN_PROGRESS
				|| action.getStatus() == AutomationExecutionStatus.RETRY_SCHEDULED)) {
			executionRepository.markRetryScheduled(tenantId, executionId, true, "Automation action retry scheduled");
			return;
		}
		
		if (!actions.isEmpty() && actions.stream().allMatch(action -> action.getStatus() == AutomationExecutionStatus.SUCCESS)) {
			executionRepository.markSuccess(tenantId, executionId, true);
		}
	}
}
