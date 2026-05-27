package com.umityasincoban.insightflow.outbox.application;

import com.umityasincoban.insightflow.feedback.domain.Feedback;

import java.util.LinkedHashMap;
import java.util.Map;

public final class OutboxPayloadFactory {
	
	private OutboxPayloadFactory() {
	}
	
	public static Map<String, Object> feedbackCreatedPayload(Feedback feedback) {
		Map<String, Object> payload = new LinkedHashMap<>();
		
		payload.put("feedbackId", feedback.getId().value().toString());
		payload.put("tenantId", feedback.getTenantId().value().toString());
		
		if (feedback.getCustomerId() != null) {
			payload.put("customerId", feedback.getCustomerId().value().toString());
		}
		
		payload.put("source", feedback.getSource().name());
		payload.put("status", feedback.getStatus().name());
		payload.put("priority", feedback.getPriority().name());
		payload.put("title", feedback.getTitle());
		payload.put("createdAt", feedback.getCreatedAt().toString());
		
		return Map.copyOf(payload);
	}
}