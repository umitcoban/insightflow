package com.umityasincoban.insightflow.search.application;

import com.umityasincoban.insightflow.feedback.domain.FeedbackPriority;
import com.umityasincoban.insightflow.feedback.domain.FeedbackRiskLevel;
import com.umityasincoban.insightflow.feedback.domain.FeedbackSentiment;
import com.umityasincoban.insightflow.feedback.domain.FeedbackSource;
import com.umityasincoban.insightflow.feedback.domain.FeedbackStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

public record FeedbackSearchQuery(
		String q,
		FeedbackStatus status,
		FeedbackPriority priority,
		FeedbackSentiment sentiment,
		FeedbackRiskLevel riskLevel,
		String category,
		FeedbackSource source,
		UUID customerId,
		OffsetDateTime from,
		OffsetDateTime to,
		int page,
		int size
) {
	
	public static FeedbackSearchQuery of(
			String q,
			FeedbackStatus status,
			FeedbackPriority priority,
			FeedbackSentiment sentiment,
			FeedbackRiskLevel riskLevel,
			String category,
			FeedbackSource source,
			UUID customerId,
			OffsetDateTime from,
			OffsetDateTime to,
			Integer page,
			Integer size
	) {
		int resolvedPage = page == null || page < 0 ? 0 : page;
		int resolvedSize = size == null || size < 1 ? 20 : Math.min(size, 100);
		return new FeedbackSearchQuery(q, status, priority, sentiment, riskLevel, category, source, customerId, from, to, resolvedPage, resolvedSize);
	}
}

