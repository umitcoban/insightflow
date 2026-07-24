package com.umityasincoban.insightflow.feedback.api;

import jakarta.validation.constraints.NotBlank;

public record CreateFeedbackNoteRequest(
		@NotBlank(message = "Feedback note content is required")
		String content
) {
}

