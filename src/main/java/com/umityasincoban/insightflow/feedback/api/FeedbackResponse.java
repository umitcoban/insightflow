package com.umityasincoban.insightflow.feedback.api;

import com.umityasincoban.insightflow.feedback.domain.Feedback;
import com.umityasincoban.insightflow.feedback.domain.FeedbackPriority;
import com.umityasincoban.insightflow.feedback.domain.FeedbackRiskLevel;
import com.umityasincoban.insightflow.feedback.domain.FeedbackSentiment;
import com.umityasincoban.insightflow.feedback.domain.FeedbackSource;
import com.umityasincoban.insightflow.feedback.domain.FeedbackStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

public record FeedbackResponse(
		UUID id,
		UUID tenantId,
		UUID customerId,
		FeedbackSource source,
		String title,
		String content,
		FeedbackStatus status,
		FeedbackPriority priority,
		FeedbackSentiment sentiment,
		String category,
		FeedbackRiskLevel riskLevel,
		String aiSummary,
		String suggestedAction,
		OffsetDateTime createdAt,
		OffsetDateTime updatedAt
) {
	
	public static FeedbackResponse from(Feedback feedback) {
		return new FeedbackResponse(
				feedback.getId().value(),
				feedback.getTenantId().value(),
				feedback.getCustomerId() == null ? null : feedback.getCustomerId().value(),
				feedback.getSource(),
				feedback.getTitle(),
				feedback.getContent(),
				feedback.getStatus(),
				feedback.getPriority(),
				feedback.getSentiment(),
				feedback.getCategory(),
				feedback.getRiskLevel(),
				feedback.getAiSummary(),
				feedback.getSuggestedAction(),
				feedback.getCreatedAt(),
				feedback.getUpdatedAt()
		);
	}
}