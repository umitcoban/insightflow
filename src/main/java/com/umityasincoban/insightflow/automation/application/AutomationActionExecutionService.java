package com.umityasincoban.insightflow.automation.application;

import com.umityasincoban.insightflow.automation.domain.AutomationActionExecutionRepository;
import com.umityasincoban.insightflow.automation.domain.AutomationExecutionId;
import com.umityasincoban.insightflow.automation.domain.AutomationRuleId;
import com.umityasincoban.insightflow.outbox.application.OutboxEventMessage;
import com.umityasincoban.insightflow.tenancy.domain.TenantId;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class AutomationActionExecutionService {
	
	private static final Set<String> SENSITIVE_HEADERS = Set.of(
			"authorization",
			"proxy-authorization",
			"x-api-key",
			"cookie",
			"set-cookie"
	);
	
	private final List<AutomationActionExecutor> automationActionExecutors;
	private final AutomationWebhookActionOrchestrator automationWebhookActionOrchestrator;
	private final AutomationActionExecutionRepository automationActionExecutionRepository;
	
	public AutomationActionExecutionService(
			List<AutomationActionExecutor> automationActionExecutors,
			AutomationWebhookActionOrchestrator automationWebhookActionOrchestrator,
			AutomationActionExecutionRepository automationActionExecutionRepository
	) {
		this.automationActionExecutors = List.copyOf(automationActionExecutors);
		this.automationWebhookActionOrchestrator = automationWebhookActionOrchestrator;
		this.automationActionExecutionRepository = automationActionExecutionRepository;
	}
	
	public List<AutomationActionExecutionResult> executeActions(
			TenantId tenantId,
			AutomationRuleId ruleId,
			AutomationExecutionId executionId,
			UUID sourceEventId,
			OutboxEventMessage eventMessage,
			List<Map<String, Object>> actions
	) {
		return actions.stream()
				.map(action -> executeAction(tenantId, ruleId, executionId, sourceEventId, eventMessage, action))
				.toList();
	}
	
	private AutomationActionExecutionResult executeAction(
			TenantId tenantId,
			AutomationRuleId ruleId,
			AutomationExecutionId executionId,
			UUID sourceEventId,
			OutboxEventMessage eventMessage,
			Map<String, Object> action
	) {
		AutomationActionExecutionResult result;
		
		try {
			String actionType = resolveActionType(action);
			if ("WEBHOOK".equals(actionType)) {
				result = automationWebhookActionOrchestrator.executeInitial(
						tenantId,
						ruleId,
						executionId,
						sourceEventId,
						eventMessage,
						action
				);
				if (!result.requestPayload().isEmpty()) {
					return result;
				}
			} else {
			AutomationActionExecutor executor = automationActionExecutors.stream()
					.filter(candidate -> candidate.supports(actionType))
					.findFirst()
					.orElse(null);
			
			if (executor == null) {
				result = AutomationActionExecutionResult.failed(
						actionType,
						"Unsupported automation action type: " + actionType
				);
			} else {
				result = executor.execute(tenantId, ruleId, sourceEventId, eventMessage, action);
			}
			}
		} catch (RuntimeException exception) {
			result = AutomationActionExecutionResult.failed(
					resolveActionType(action),
					exception.getMessage() == null ? "Automation action failed" : exception.getMessage()
			);
		}
		
		if (result.success()) {
			automationActionExecutionRepository.saveSuccess(
					tenantId,
					executionId,
					result.actionType(),
					resolveRequestPayload(action, result),
					result.resultPayload()
			);
		} else {
			automationActionExecutionRepository.saveFailed(
					tenantId,
					executionId,
					result.actionType(),
					resolveRequestPayload(action, result),
					result.resultPayload(),
					result.errorMessage()
			);
		}
		
		return result;
	}
	
	private static Map<String, Object> resolveRequestPayload(
			Map<String, Object> action,
			AutomationActionExecutionResult result
	) {
		if (result.requestPayload() == null || result.requestPayload().isEmpty()) {
			return action == null ? Map.of() : sanitizeFallbackAction(action);
		}
		
		return result.requestPayload();
	}
	
	@SuppressWarnings("unchecked")
	private static Map<String, Object> sanitizeFallbackAction(Map<String, Object> action) {
		if (!"WEBHOOK".equals(resolveActionType(action))) {
			return action;
		}
		
		Map<String, Object> sanitized = new LinkedHashMap<>(action);
		Object headersValue = sanitized.get("headers");
		if (headersValue instanceof Map<?, ?> headers) {
			Map<String, Object> sanitizedHeaders = new LinkedHashMap<>();
			for (Map.Entry<?, ?> entry : headers.entrySet()) {
				String headerName = entry.getKey() == null ? "" : entry.getKey().toString();
				if (SENSITIVE_HEADERS.contains(headerName.toLowerCase(Locale.ROOT))) {
					sanitizedHeaders.put(headerName, "***");
				} else {
					sanitizedHeaders.put(headerName, entry.getValue());
				}
			}
			sanitized.put("headers", sanitizedHeaders);
			sanitized.put("retryEligible", false);
			sanitized.put("retryIneligibleReason", "SENSITIVE_HEADERS_NOT_PERSISTED");
		}
		return sanitized;
	}
	
	private static String resolveActionType(Map<String, Object> action) {
		if (action == null || action.get("type") == null) {
			return "UNKNOWN";
		}
		
		return action.get("type").toString();
	}
}
