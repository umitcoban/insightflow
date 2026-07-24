package com.umityasincoban.insightflow.knowledge.api;

import com.umityasincoban.insightflow.knowledge.domain.KnowledgeDocument;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

public record KnowledgeDocumentResponse(
		UUID id,
		UUID tenantId,
		String title,
		String source,
		Map<String, Object> metadata,
		OffsetDateTime createdAt,
		OffsetDateTime updatedAt
) {
	
	public static KnowledgeDocumentResponse from(KnowledgeDocument document) {
		return new KnowledgeDocumentResponse(
				document.id(),
				document.tenantId().value(),
				document.title(),
				document.source(),
				document.metadata(),
				document.createdAt(),
				document.updatedAt()
		);
	}
}

