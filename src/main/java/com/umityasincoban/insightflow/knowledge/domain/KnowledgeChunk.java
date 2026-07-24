package com.umityasincoban.insightflow.knowledge.domain;

import com.umityasincoban.insightflow.tenancy.domain.TenantId;

import java.time.OffsetDateTime;
import java.util.UUID;

public record KnowledgeChunk(
		UUID id,
		TenantId tenantId,
		UUID documentId,
		int chunkIndex,
		String content,
		OffsetDateTime createdAt
) {
}

