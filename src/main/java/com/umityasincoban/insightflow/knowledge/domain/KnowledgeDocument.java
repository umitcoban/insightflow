package com.umityasincoban.insightflow.knowledge.domain;

import com.umityasincoban.insightflow.tenancy.domain.TenantId;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

public record KnowledgeDocument(
		UUID id,
		TenantId tenantId,
		String title,
		String source,
		String content,
		Map<String, Object> metadata,
		OffsetDateTime createdAt,
		OffsetDateTime updatedAt
) {
}

