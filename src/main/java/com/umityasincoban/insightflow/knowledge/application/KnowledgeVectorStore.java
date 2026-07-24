package com.umityasincoban.insightflow.knowledge.application;

import com.umityasincoban.insightflow.knowledge.domain.KnowledgeChunk;
import com.umityasincoban.insightflow.tenancy.domain.TenantId;

import java.util.List;
import java.util.UUID;

public interface KnowledgeVectorStore {
	
	void index(KnowledgeChunk chunk, String documentTitle, String source, List<Double> embedding);
	
	void deleteDocument(TenantId tenantId, UUID documentId);
	
	List<RetrievedKnowledgeChunk> search(TenantId tenantId, List<Double> queryEmbedding, int limit);
}

