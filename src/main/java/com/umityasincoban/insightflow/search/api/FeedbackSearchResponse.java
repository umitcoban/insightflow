package com.umityasincoban.insightflow.search.api;

import com.umityasincoban.insightflow.search.application.FeedbackSearchDocument;

import java.time.OffsetDateTime;
import java.util.UUID;

public record FeedbackSearchResponse(
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
	
	public static FeedbackSearchResponse from(FeedbackSearchDocument document) {
		return new FeedbackSearchResponse(
				document.id(),
				document.tenantId(),
				document.customerId(),
				document.source(),
				document.title(),
				document.content(),
				document.status(),
				document.priority(),
				document.sentiment(),
				document.category(),
				document.riskLevel(),
				document.aiSummary(),
				document.suggestedAction(),
				document.createdAt(),
				document.updatedAt()
		);
	}
}

