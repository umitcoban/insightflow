package com.umityasincoban.insightflow.automation.application;

import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Component
public class AutomationWebhookPayloadSanitizer {
	
	private static final String MASK = "***";
	private static final Set<String> SENSITIVE_HEADERS = Set.of(
			"authorization",
			"proxy-authorization",
			"x-api-key",
			"cookie",
			"set-cookie"
	);
	
	public Map<String, Object> sanitizeRequestPayload(
			String url,
			String method,
			Map<String, String> headers,
			Object body,
			int timeoutMs
	) {
		Map<String, Object> payload = new LinkedHashMap<>();
		payload.put("type", "WEBHOOK");
		payload.put("url", url);
		payload.put("method", method);
		payload.put("headers", sanitizeHeaders(headers));
		if (body != null) {
			payload.put("body", body);
		}
		payload.put("timeoutMs", timeoutMs);
		boolean retryEligible = !containsSensitiveHeader(headers);
		payload.put("retryEligible", retryEligible);
		if (!retryEligible) {
			payload.put("retryIneligibleReason", "SENSITIVE_HEADERS_NOT_PERSISTED");
		}
		return payload;
	}
	
	public Map<String, String> sanitizeHeaders(Map<String, String> headers) {
		Map<String, String> sanitized = new LinkedHashMap<>();
		for (Map.Entry<String, String> entry : headers.entrySet()) {
			if (isSensitive(entry.getKey())) {
				sanitized.put(entry.getKey(), MASK);
			} else {
				sanitized.put(entry.getKey(), entry.getValue());
			}
		}
		return sanitized;
	}
	
	private static boolean isSensitive(String headerName) {
		return headerName != null && SENSITIVE_HEADERS.contains(headerName.toLowerCase(Locale.ROOT));
	}
	
	public boolean containsSensitiveHeader(Map<String, String> headers) {
		return headers.keySet().stream().anyMatch(AutomationWebhookPayloadSanitizer::isSensitive);
	}
}
