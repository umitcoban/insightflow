package com.umityasincoban.insightflow.outbox.application;

import com.umityasincoban.insightflow.feedback.domain.Feedback;
import com.umityasincoban.insightflow.ai.domain.FeedbackAiAnalysisResult;
import com.umityasincoban.insightflow.feedback.domain.FeedbackId;
import com.umityasincoban.insightflow.tenancy.domain.TenantId;

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
	
	public static Map<String, Object> feedbackAiAnalysisCompletedPayload(
			TenantId tenantId,
			FeedbackId feedbackId,
			FeedbackAiAnalysisResult result
	) {
		Map<String, Object> payload = new LinkedHashMap<>();
		
		payload.put("tenantId", tenantId.value().toString());
		payload.put("feedbackId", feedbackId.value().toString());
		payload.put("sentiment", result.sentiment().name());
		payload.put("category", result.category());
		payload.put("riskLevel", result.riskLevel().name());
		payload.put("summary", result.summary());
		payload.put("suggestedAction", result.suggestedAction());
		
		return Map.copyOf(payload);
	}
}