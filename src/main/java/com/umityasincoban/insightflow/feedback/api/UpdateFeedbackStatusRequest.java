package com.umityasincoban.insightflow.feedback.api;

import com.umityasincoban.insightflow.feedback.domain.FeedbackStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateFeedbackStatusRequest(
		@NotNull(message = "Feedback status is required")
		FeedbackStatus status
) {
}

