package com.umityasincoban.insightflow.automation.application;

import com.umityasincoban.insightflow.automation.domain.AutomationRuleId;
import com.umityasincoban.insightflow.outbox.application.OutboxEventMessage;
import com.umityasincoban.insightflow.tenancy.domain.TenantId;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Component
public class AutomationWebhookActionExecutor implements AutomationActionExecutor {
	
	private static final String WEBHOOK_ACTION_TYPE = "WEBHOOK";
	private static final Set<String> SUPPORTED_METHODS = Set.of("GET", "POST", "PUT", "PATCH");
	
	private final AutomationWebhookClient webhookClient;
	private final AutomationWebhookUrlValidator urlValidator;
	private final AutomationWebhookTemplateResolver templateResolver;
	private final AutomationWebhookPayloadSanitizer payloadSanitizer;
	private final AutomationWebhookProperties properties;
	
	public AutomationWebhookActionExecutor(
			AutomationWebhookClient webhookClient,
			AutomationWebhookUrlValidator urlValidator,
			AutomationWebhookTemplateResolver templateResolver,
			AutomationWebhookPayloadSanitizer payloadSanitizer,
			AutomationWebhookProperties properties
	) {
		this.webhookClient = webhookClient;
		this.urlValidator = urlValidator;
		this.templateResolver = templateResolver;
		this.payloadSanitizer = payloadSanitizer;
		this.properties = properties;
	}
	
	@Override
	public boolean supports(String actionType) {
		return WEBHOOK_ACTION_TYPE.equals(actionType);
	}
	
	@Override
	public AutomationActionExecutionResult execute(
			TenantId tenantId,
			AutomationRuleId ruleId,
			UUID sourceEventId,
			OutboxEventMessage eventMessage,
			Map<String, Object> actionJson
	) {
		try {
			AutomationWebhookExecutionPlan plan = prepare(actionJson, eventMessage);
			return executePrepared(plan.request(), plan.requestPayload());
		} catch (RuntimeException exception) {
			return AutomationActionExecutionResult.failed(
					WEBHOOK_ACTION_TYPE,
					Map.of(),
					failurePayload(exception),
					exception.getMessage() == null ? "Webhook action failed" : exception.getMessage()
			);
		}
	}
	
	public AutomationWebhookExecutionPlan prepare(Map<String, Object> actionJson, OutboxEventMessage eventMessage) {
		validateActionType(actionJson);
		URI url = urlValidator.validate(stringValue(actionJson.get("url")));
		String method = resolveMethod(actionJson.get("method"));
		Map<String, String> headers = templateResolver.resolveHeaders(resolveHeaders(actionJson.get("headers")), eventMessage);
		Object body = "GET".equals(method) ? null : templateResolver.resolve(actionJson.get("body"), eventMessage);
		int timeoutMs = resolveTimeout(actionJson.get("timeoutMs"));
		
		return new AutomationWebhookExecutionPlan(
				new AutomationWebhookRequest(url, method, headers, body, timeoutMs),
				payloadSanitizer.sanitizeRequestPayload(url.toString(), method, headers, body, timeoutMs)
		);
	}
	
	public AutomationActionExecutionResult executePrepared(
			AutomationWebhookRequest request,
			Map<String, Object> requestPayload
	) {
		try {
			urlValidator.validateBeforeSend(request.url().toString());
			AutomationWebhookResponse response = webhookClient.send(request);
			Map<String, Object> resultPayload = responsePayload(response.success(), response.httpStatus(), response.responseBody(), response.durationMs(), response.retryAfter());
			if (response.success()) {
				return AutomationActionExecutionResult.success(WEBHOOK_ACTION_TYPE, requestPayload, resultPayload);
			}
			
			return AutomationActionExecutionResult.failed(
					WEBHOOK_ACTION_TYPE,
					requestPayload,
					resultPayload,
					"Webhook request returned non-2xx status: " + response.httpStatus()
			);
		} catch (AutomationWebhookRequestException exception) {
			return AutomationActionExecutionResult.failed(
					WEBHOOK_ACTION_TYPE,
					requestPayload,
					exception.getResultPayload(),
					exception.getMessage()
			);
		} catch (RuntimeException exception) {
			return AutomationActionExecutionResult.failed(
					WEBHOOK_ACTION_TYPE,
					requestPayload,
					failurePayload(exception),
					exception.getMessage() == null ? "Webhook action failed" : exception.getMessage()
			);
		}
	}
	
	private static void validateActionType(Map<String, Object> actionJson) {
		if (actionJson == null || !WEBHOOK_ACTION_TYPE.equals(stringValue(actionJson.get("type")))) {
			throw new InvalidAutomationWebhookConfigurationException("Webhook action type must be WEBHOOK");
		}
	}
	
	private static String resolveMethod(Object value) {
		String method = value == null || value.toString().isBlank()
				? "POST"
				: value.toString().trim().toUpperCase(Locale.ROOT);
		if (!SUPPORTED_METHODS.contains(method)) {
			throw new InvalidAutomationWebhookConfigurationException("Unsupported webhook HTTP method: " + method);
		}
		return method;
	}
	
	private int resolveTimeout(Object value) {
		int timeoutMs = properties.getDefaultTimeoutMs();
		if (value instanceof Number number) {
			timeoutMs = number.intValue();
		} else if (value != null && !value.toString().isBlank()) {
			try {
				timeoutMs = Integer.parseInt(value.toString());
			} catch (NumberFormatException exception) {
				throw new InvalidAutomationWebhookConfigurationException("Webhook timeoutMs must be a number");
			}
		}
		
		if (timeoutMs < properties.getMinTimeoutMs()) {
			return properties.getMinTimeoutMs();
		}
		return Math.min(timeoutMs, properties.getMaxTimeoutMs());
	}
	
	private static Map<String, String> resolveHeaders(Object value) {
		if (value == null) {
			return Map.of();
		}
		
		if (!(value instanceof Map<?, ?> map)) {
			throw new InvalidAutomationWebhookConfigurationException("Webhook headers must be an object");
		}
		
		Map<String, String> headers = new LinkedHashMap<>();
		for (Map.Entry<?, ?> entry : map.entrySet()) {
			if (entry.getKey() != null && entry.getValue() != null) {
				headers.put(entry.getKey().toString(), entry.getValue().toString());
			}
		}
		return headers;
	}
	
	private Map<String, Object> responsePayload(
			boolean success,
			int httpStatus,
			String responseBody,
			long durationMs,
			String retryAfter
	) {
		Map<String, Object> payload = new LinkedHashMap<>();
		payload.put("success", success);
		payload.put("httpStatus", httpStatus);
		putBoundedResponseBody(payload, responseBody);
		payload.put("durationMs", durationMs);
		if (retryAfter != null && !retryAfter.isBlank()) {
			payload.put("retryAfter", retryAfter);
		}
		return payload;
	}
	
	private static Map<String, Object> failurePayload(RuntimeException exception) {
		String errorType = "CONFIGURATION_ERROR";
		if (exception instanceof UnsafeAutomationWebhookUrlException) {
			errorType = "UNSAFE_URL";
		} else if (exception instanceof AutomationWebhookDnsResolutionException) {
			errorType = "DNS_RESOLUTION_FAILED";
		} else if (exception instanceof InvalidAutomationWebhookConfigurationException) {
			errorType = "CONFIGURATION_ERROR";
		}
		return Map.of("success", false, "errorType", errorType);
	}
	
	private void putBoundedResponseBody(Map<String, Object> payload, String responseBody) {
		if (responseBody == null) {
			payload.put("responseBody", "");
			payload.put("responseBodyTruncated", false);
			return;
		}
		
		int maxLength = properties.getMaxResponseBodyLength();
		if (responseBody.length() > maxLength) {
			payload.put("responseBody", responseBody.substring(0, maxLength));
			payload.put("responseBodyTruncated", true);
		} else {
			payload.put("responseBody", responseBody);
			payload.put("responseBodyTruncated", false);
		}
	}
	
	private static String stringValue(Object value) {
		return value == null ? null : value.toString();
	}
}
