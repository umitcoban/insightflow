package com.umityasincoban.insightflow.feedback.api;

import com.umityasincoban.insightflow.feedback.domain.FeedbackPriority;
import jakarta.validation.constraints.NotNull;

public record UpdateFeedbackPriorityRequest(
		@NotNull(message = "Feedback priority is required")
		FeedbackPriority priority
) {
}

