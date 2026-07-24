package com.umityasincoban.insightflow.knowledge.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Map;

public record CreateKnowledgeDocumentRequest(
		@NotBlank(message = "Knowledge document title is required")
		@Size(max = 240, message = "Knowledge document title cannot be longer than 240 characters")
		String title,
		@NotBlank(message = "Knowledge document source is required")
		@Size(max = 80, message = "Knowledge document source cannot be longer than 80 characters")
		String source,
		@NotBlank(message = "Knowledge document content is required")
		String content,
		Map<String, Object> metadata
) {
}

