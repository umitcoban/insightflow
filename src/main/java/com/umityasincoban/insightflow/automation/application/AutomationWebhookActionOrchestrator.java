package com.umityasincoban.insightflow.automation.application;

import com.umityasincoban.insightflow.automation.domain.AutomationActionExecution;
import com.umityasincoban.insightflow.automation.domain.AutomationExecutionId;
import com.umityasincoban.insightflow.automation.domain.AutomationRuleId;
import com.umityasincoban.insightflow.outbox.application.OutboxEventMessage;
import com.umityasincoban.insightflow.tenancy.domain.TenantId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class AutomationWebhookActionOrchestrator {
	
	private final AutomationWebhookActionExecutor webhookActionExecutor;
	private final AutomationActionExecutionPersistenceService persistenceService;
	private final AutomationWebhookRetryPolicy retryPolicy;
	private final AutomationWebhookProperties properties;
	private final AutomationWebhookUrlValidator urlValidator;
	
	public AutomationWebhookActionOrchestrator(
			AutomationWebhookActionExecutor webhookActionExecutor,
			AutomationActionExecutionPersistenceService persistenceService,
			AutomationWebhookRetryPolicy retryPolicy,
			AutomationWebhookProperties properties,
			AutomationWebhookUrlValidator urlValidator
	) {
		this.webhookActionExecutor = webhookActionExecutor;
		this.persistenceService = persistenceService;
		this.retryPolicy = retryPolicy;
		this.properties = properties;
		this.urlValidator = urlValidator;
	}
	
	public AutomationActionExecutionResult executeInitial(
			TenantId tenantId,
			AutomationRuleId ruleId,
			AutomationExecutionId executionId,
			UUID sourceEventId,
			OutboxEventMessage eventMessage,
			Map<String, Object> actionJson
	) {
		try {
			AutomationWebhookExecutionPlan plan = webhookActionExecutor.prepare(actionJson, eventMessage);
			AutomationActionExecution actionExecution = persistenceService.createPendingWebhookAction(
					tenantId,
					executionId,
					plan.requestPayload(),
					maxAttempts(plan.requestPayload())
			);
			return executeAttempt(tenantId, executionId, actionExecution, plan.request(), plan.requestPayload());
		} catch (RuntimeException exception) {
			return AutomationActionExecutionResult.failed(
					"WEBHOOK",
					Map.of(),
					failurePayload(exception),
					exception.getMessage() == null ? "Webhook action failed" : exception.getMessage()
			);
		}
	}
	
	public AutomationActionExecutionResult executeRetry(AutomationActionExecution actionExecution) {
		try {
			AutomationWebhookRequest request = requestFromPayload(actionExecution.getRequestPayload());
			return executeAttempt(
					actionExecution.getTenantId(),
					actionExecution.getExecutionId(),
					actionExecution,
					request,
					actionExecution.getRequestPayload()
			);
		} catch (RuntimeException exception) {
			Map<String, Object> resultPayload = withAttempt(failurePayload(exception), actionExecution.getAttemptCount());
			AutomationWebhookRetryDecision decision = retryDecision(actionExecution.getRequestPayload(), resultPayload, actionExecution);
			if (decision.retryable()) {
				Map<String, Object> scheduledPayload = withRetry(resultPayload, decision);
				persistenceService.scheduleRetry(
						actionExecution.getTenantId(),
						actionExecution.getExecutionId(),
						actionExecution.getId(),
						scheduledPayload,
						exception.getMessage(),
						decision.nextRetryAt()
				);
				return AutomationActionExecutionResult.failed("WEBHOOK", actionExecution.getRequestPayload(), scheduledPayload, exception.getMessage());
			}
			
			Map<String, Object> failedPayload = withRetry(resultPayload, decision);
			persistenceService.completeFailure(
					actionExecution.getTenantId(),
					actionExecution.getExecutionId(),
					actionExecution.getId(),
					failedPayload,
					exception.getMessage(),
					OffsetDateTime.now()
			);
			return AutomationActionExecutionResult.failed("WEBHOOK", actionExecution.getRequestPayload(), failedPayload, exception.getMessage());
		}
	}
	
	private AutomationActionExecutionResult executeAttempt(
			TenantId tenantId,
			AutomationExecutionId executionId,
			AutomationActionExecution actionExecution,
			AutomationWebhookRequest request,
			Map<String, Object> requestPayload
	) {
		OffsetDateTime now = OffsetDateTime.now();
		AutomationActionExecution started = actionExecution.getStatus().name().equals("IN_PROGRESS")
				? actionExecution
				: persistenceService.markAttemptStarted(tenantId, actionExecution.getId(), now);
		
		AutomationActionExecutionResult result = webhookActionExecutor.executePrepared(request, requestPayload);
		Map<String, Object> resultPayload = withAttempt(result.resultPayload(), started.getAttemptCount());
		if (!Boolean.TRUE.equals(requestPayload.get("retryEligible")) && requestPayload.get("retryIneligibleReason") != null) {
			resultPayload = new LinkedHashMap<>(resultPayload);
			resultPayload.put("retryIneligibleReason", requestPayload.get("retryIneligibleReason"));
		}
		
		if (result.success()) {
			persistenceService.completeSuccess(tenantId, executionId, started.getId(), resultPayload, OffsetDateTime.now());
			return AutomationActionExecutionResult.success("WEBHOOK", requestPayload, resultPayload);
		}
		
		AutomationWebhookRetryDecision decision = retryDecision(requestPayload, resultPayload, started);
		if (decision.retryable()) {
			Map<String, Object> scheduledPayload = withRetry(resultPayload, decision);
			persistenceService.scheduleRetry(
					tenantId,
					executionId,
					started.getId(),
					scheduledPayload,
					result.errorMessage(),
					decision.nextRetryAt()
			);
			return AutomationActionExecutionResult.failed("WEBHOOK", requestPayload, scheduledPayload, result.errorMessage());
		}
		
		Map<String, Object> failedPayload = withRetry(resultPayload, decision);
		persistenceService.completeFailure(
				tenantId,
				executionId,
				started.getId(),
				failedPayload,
				result.errorMessage(),
				OffsetDateTime.now()
		);
		return AutomationActionExecutionResult.failed("WEBHOOK", requestPayload, failedPayload, result.errorMessage());
	}
	
	private AutomationWebhookRetryDecision retryDecision(
			Map<String, Object> requestPayload,
			Map<String, Object> resultPayload,
			AutomationActionExecution actionExecution
	) {
		if (!Boolean.TRUE.equals(requestPayload.get("retryEligible"))) {
			return new AutomationWebhookRetryDecision(false, false, null);
		}
		return retryPolicy.decide(resultPayload, actionExecution.getAttemptCount(), actionExecution.getMaxAttempts(), OffsetDateTime.now());
	}
	
	private int maxAttempts(Map<String, Object> requestPayload) {
		if (!properties.getRetry().isEnabled() || !Boolean.TRUE.equals(requestPayload.get("retryEligible"))) {
			return 1;
		}
		return properties.getRetry().getMaxAttempts();
	}
	
	private static Map<String, Object> withAttempt(Map<String, Object> resultPayload, int attempt) {
		Map<String, Object> payload = new LinkedHashMap<>(resultPayload);
		payload.put("attempt", attempt);
		payload.put("transactionActiveDuringHttp", TransactionSynchronizationManager.isActualTransactionActive());
		return payload;
	}
	
	private static Map<String, Object> withRetry(
			Map<String, Object> resultPayload,
			AutomationWebhookRetryDecision decision
	) {
		Map<String, Object> payload = new LinkedHashMap<>(resultPayload);
		payload.put("retryable", decision.retryable());
		payload.put("retriesExhausted", decision.retriesExhausted());
		if (decision.nextRetryAt() != null) {
			payload.put("nextRetryAt", decision.nextRetryAt().toString());
		}
		return payload;
	}
	
	@SuppressWarnings("unchecked")
	private AutomationWebhookRequest requestFromPayload(Map<String, Object> requestPayload) {
		URI url = urlValidator.validate(requestPayload.get("url").toString());
		Map<String, String> headers = new LinkedHashMap<>();
		Object headerValue = requestPayload.get("headers");
		if (headerValue instanceof Map<?, ?> map) {
			for (Map.Entry<?, ?> entry : map.entrySet()) {
				headers.put(entry.getKey().toString(), entry.getValue().toString());
			}
		}
		return new AutomationWebhookRequest(
				url,
				requestPayload.get("method").toString(),
				headers,
				requestPayload.get("body"),
				((Number) requestPayload.get("timeoutMs")).intValue()
		);
	}
	
	private static Map<String, Object> failurePayload(RuntimeException exception) {
		String errorType = "CONFIGURATION_ERROR";
		if (exception instanceof UnsafeAutomationWebhookUrlException) {
			errorType = "UNSAFE_URL";
		} else if (exception instanceof AutomationWebhookDnsResolutionException) {
			errorType = "DNS_RESOLUTION_FAILED";
		}
		return Map.of("success", false, "errorType", errorType, "retryable", "DNS_RESOLUTION_FAILED".equals(errorType));
	}
}
