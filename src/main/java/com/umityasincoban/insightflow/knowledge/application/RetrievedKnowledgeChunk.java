package com.umityasincoban.insightflow.knowledge.application;

import java.util.UUID;

public record RetrievedKnowledgeChunk(
		UUID documentId,
		UUID chunkId,
		String documentTitle,
		String source,
		String content,
		double score
) {
}

