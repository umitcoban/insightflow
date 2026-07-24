package com.umityasincoban.insightflow.knowledge.application;

import java.util.List;

public record AssistantAnswer(
		String answer,
		List<RetrievedKnowledgeChunk> sources
) {
}

