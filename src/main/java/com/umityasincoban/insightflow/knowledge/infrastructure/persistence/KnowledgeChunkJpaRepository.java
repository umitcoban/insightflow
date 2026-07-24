package com.umityasincoban.insightflow.knowledge.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface KnowledgeChunkJpaRepository extends JpaRepository<KnowledgeChunkEntity, UUID> {
	
	List<KnowledgeChunkEntity> findByTenantIdAndDocumentIdOrderByChunkIndexAsc(UUID tenantId, UUID documentId);
	
	void deleteByTenantIdAndDocumentId(UUID tenantId, UUID documentId);
}

