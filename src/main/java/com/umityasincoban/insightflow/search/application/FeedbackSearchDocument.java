package com.umityasincoban.insightflow.search.application;

import com.umityasincoban.insightflow.feedback.domain.Feedback;

import java.time.OffsetDateTime;
import java.util.UUID;

public record FeedbackSearchDocument(
		UUID id,
		UUID tenantId,
		UUID customerId,
		String source,
		String title,
		String content,
		String status,
		String priority,
		String sentiment,
		String category,
		String riskLevel,
		String aiSummary,
		String suggestedAction,
		OffsetDateTime createdAt,
		OffsetDateTime updatedAt
) {
	
	public static FeedbackSearchDocument from(Feedback feedback) {
		return new FeedbackSearchDocument(
				feedback.getId().value(),
				feedback.getTenantId().value(),
				feedback.getCustomerId() == null ? null : feedback.getCustomerId().value(),
				feedback.getSource().name(),
				feedback.getTitle(),
				feedback.getContent(),
				feedback.getStatus().name(),
				feedback.getPriority().name(),
				feedback.getSentiment() == null ? null : feedback.getSentiment().name(),
				feedback.getCategory(),
				feedback.getRiskLevel() == null ? null : feedback.getRiskLevel().name(),
				feedback.getAiSummary(),
				feedback.getSuggestedAction(),
				feedback.getCreatedAt(),
				feedback.getUpdatedAt()
		);
	}
}

