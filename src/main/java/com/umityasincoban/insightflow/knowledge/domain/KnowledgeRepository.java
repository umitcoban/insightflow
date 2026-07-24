package com.umityasincoban.insightflow.knowledge.domain;

import com.umityasincoban.insightflow.tenancy.domain.TenantId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface KnowledgeRepository {
	
	KnowledgeDocument saveDocument(TenantId tenantId, String title, String source, String content, Map<String, Object> metadata);
	
	KnowledgeChunk saveChunk(TenantId tenantId, UUID documentId, int chunkIndex, String content);
	
	Page<KnowledgeDocument> findByTenantId(TenantId tenantId, Pageable pageable);
	
	Optional<KnowledgeDocument> findByTenantIdAndId(TenantId tenantId, UUID documentId);
	
	List<KnowledgeChunk> findChunksByTenantIdAndDocumentId(TenantId tenantId, UUID documentId);
	
	void deleteByTenantIdAndId(TenantId tenantId, UUID documentId);
}

