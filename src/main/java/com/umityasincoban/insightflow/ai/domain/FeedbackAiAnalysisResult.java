package com.umityasincoban.insightflow.ai.domain;

import com.umityasincoban.insightflow.feedback.domain.FeedbackRiskLevel;
import com.umityasincoban.insightflow.feedback.domain.FeedbackSentiment;

public record FeedbackAiAnalysisResult(
		FeedbackSentiment sentiment,
		String category,
		FeedbackRiskLevel riskLevel,
		String summary,
		String suggestedAction
) {
}