package com.umityasincoban.insightflow.knowledge.infrastructure.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface KnowledgeDocumentJpaRepository extends JpaRepository<KnowledgeDocumentEntity, UUID> {
	
	Page<KnowledgeDocumentEntity> findByTenantId(UUID tenantId, Pageable pageable);
	
	Optional<KnowledgeDocumentEntity> findByTenantIdAndId(UUID tenantId, UUID id);
	
	void deleteByTenantIdAndId(UUID tenantId, UUID id);
}

