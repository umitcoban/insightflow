package com.umityasincoban.insightflow.feedback.api;

import com.umityasincoban.insightflow.feedback.domain.FeedbackPriority;
import com.umityasincoban.insightflow.feedback.domain.FeedbackSource;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Map;
import java.util.UUID;

public record CreateFeedbackRequest(
		UUID customerId,
		
		@NotNull(message = "Feedback source is required")
		FeedbackSource source,
		
		@NotBlank(message = "Feedback title is required")
		@Size(max = 200, message = "Feedback title cannot be longer than 200 characters")
		String title,
		
		@NotBlank(message = "Feedback content is required")
		String content,
		
		@NotNull(message = "Feedback priority is required")
		FeedbackPriority priority,
		
		Map<String, Object> metadata
) {
}