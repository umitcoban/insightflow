package com.umityasincoban.insightflow.automation.application;

import com.umityasincoban.insightflow.automation.domain.AutomationExecution;
import com.umityasincoban.insightflow.automation.domain.AutomationExecutionRepository;
import com.umityasincoban.insightflow.automation.domain.AutomationRule;
import com.umityasincoban.insightflow.automation.domain.AutomationRuleRepository;
import com.umityasincoban.insightflow.automation.domain.DuplicateAutomationExecutionException;
import com.umityasincoban.insightflow.outbox.application.OutboxEventMessage;
import com.umityasincoban.insightflow.tenancy.domain.TenantId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class AutomationRuleEvaluationService {
	
	private static final Logger log = LoggerFactory.getLogger(AutomationRuleEvaluationService.class);
	
	private final AutomationRuleRepository automationRuleRepository;
	private final AutomationExecutionRepository automationExecutionRepository;
	private final AutomationConditionEvaluator automationConditionEvaluator;
	private final AutomationActionExecutionService automationActionExecutionService;
	
	public AutomationRuleEvaluationService(
			AutomationRuleRepository automationRuleRepository,
			AutomationExecutionRepository automationExecutionRepository,
			AutomationConditionEvaluator automationConditionEvaluator,
			AutomationActionExecutionService automationActionExecutionService
	) {
		this.automationRuleRepository = automationRuleRepository;
		this.automationExecutionRepository = automationExecutionRepository;
		this.automationConditionEvaluator = automationConditionEvaluator;
		this.automationActionExecutionService = automationActionExecutionService;
	}
	
	public void evaluate(OutboxEventMessage message) {
		if (!isValid(message)) {
			log.debug("Skipping malformed automation event message={}", message);
			return;
		}
		
		TenantId tenantId = TenantId.of(UUID.fromString(message.tenantId()));
		UUID sourceEventId = UUID.fromString(message.eventId());
		Map<String, Object> payload = message.payload() == null ? Map.of() : message.payload();
		
		List<AutomationRule> rules = automationRuleRepository.findActiveByTenantIdAndTriggerEventType(
				tenantId,
				message.eventType()
		);
		
		if (rules.isEmpty()) {
			log.debug(
					"No active automation rules found tenantId={} eventType={} sourceEventId={}",
					tenantId.value(),
					message.eventType(),
					sourceEventId
			);
			return;
		}
		
		for (AutomationRule rule : rules) {
			evaluateRule(tenantId, sourceEventId, message, payload, rule);
		}
	}
	
	private void evaluateRule(
			TenantId tenantId,
			UUID sourceEventId,
			OutboxEventMessage message,
			Map<String, Object> payload,
			AutomationRule rule
	) {
		if (automationExecutionRepository.existsByTenantIdAndRuleIdAndSourceEventId(
				tenantId,
				rule.getId(),
				sourceEventId
		)) {
			log.info(
					"Skipping duplicate automation execution tenantId={} ruleId={} sourceEventId={}",
					tenantId.value(),
					rule.getId().value(),
					sourceEventId
			);
			return;
		}
		
		AutomationExecution execution;
		try {
			execution = automationExecutionRepository.startExecution(
					tenantId,
					rule.getId(),
					sourceEventId,
					message.eventType()
			);
		} catch (DuplicateAutomationExecutionException exception) {
			log.info(
					"Skipping concurrently created duplicate automation execution tenantId={} ruleId={} sourceEventId={}",
					exception.getTenantId(),
					exception.getRuleId(),
					exception.getSourceEventId()
			);
			return;
		}
		
		try {
			boolean matched = automationConditionEvaluator.matches(rule.getConditionJson(), payload);
			
			if (!matched) {
				automationExecutionRepository.markSkipped(tenantId, execution.getId());
				return;
			}
			
			List<AutomationActionExecutionResult> actionResults = automationActionExecutionService.executeActions(
					tenantId,
					rule.getId(),
					execution.getId(),
					sourceEventId,
					message,
					rule.getActionJson()
			);
			
			List<String> actionErrors = actionResults.stream()
					.filter(result -> !result.success())
					.filter(result -> !Boolean.TRUE.equals(result.resultPayload().get("retryable")))
					.map(AutomationActionExecutionResult::errorMessage)
					.toList();
			boolean retryScheduled = actionResults.stream()
					.filter(result -> !result.success())
					.anyMatch(result -> Boolean.TRUE.equals(result.resultPayload().get("retryable")));
			
			if (actionErrors.isEmpty()) {
				if (retryScheduled) {
					automationExecutionRepository.markRetryScheduled(tenantId, execution.getId(), true, "Automation action retry scheduled");
				} else {
					automationExecutionRepository.markSuccess(tenantId, execution.getId(), true);
				}
			} else {
				automationExecutionRepository.markFailed(tenantId, execution.getId(), true, String.join("; ", actionErrors));
			}
		} catch (RuntimeException exception) {
			log.error(
					"Automation rule evaluation failed tenantId={} ruleId={} sourceEventId={}",
					tenantId.value(),
					rule.getId().value(),
					sourceEventId,
					exception
			);
			automationExecutionRepository.markFailed(
					tenantId,
					execution.getId(),
					false,
					exception.getMessage() == null ? "Automation rule evaluation failed" : exception.getMessage()
			);
		}
	}
	
	private static boolean isValid(OutboxEventMessage message) {
		if (message == null || isBlank(message.eventId()) || isBlank(message.tenantId()) || isBlank(message.eventType())) {
			return false;
		}
		
		try {
			UUID.fromString(message.eventId());
			UUID.fromString(message.tenantId());
			return true;
		} catch (IllegalArgumentException exception) {
			return false;
		}
	}
	
	private static boolean isBlank(String value) {
		return value == null || value.isBlank();
	}
}
