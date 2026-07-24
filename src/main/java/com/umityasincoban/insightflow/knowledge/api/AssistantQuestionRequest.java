package com.umityasincoban.insightflow.knowledge.api;

import jakarta.validation.constraints.NotBlank;

public record AssistantQuestionRequest(
		@NotBlank(message = "Question is required")
		String question
) {
}

