package com.umityasincoban.insightflow.feedback.api;

import jakarta.validation.constraints.Size;

public record AssignFeedbackRequest(
		@Size(max = 180, message = "Assigned user cannot be longer than 180 characters")
		String assignedTo
) {
}

