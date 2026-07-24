package com.umityasincoban.insightflow.knowledge.infrastructure.persistence;

import com.umityasincoban.insightflow.knowledge.domain.KnowledgeChunk;
import com.umityasincoban.insightflow.knowledge.domain.KnowledgeDocument;
import com.umityasincoban.insightflow.knowledge.domain.KnowledgeRepository;
import com.umityasincoban.insightflow.tenancy.domain.TenantId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Repository
public class JpaKnowledgeRepositoryAdapter implements KnowledgeRepository {
	
	private final KnowledgeDocumentJpaRepository documentJpaRepository;
	private final KnowledgeChunkJpaRepository chunkJpaRepository;
	
	public JpaKnowledgeRepositoryAdapter(
			KnowledgeDocumentJpaRepository documentJpaRepository,
			KnowledgeChunkJpaRepository chunkJpaRepository
	) {
		this.documentJpaRepository = documentJpaRepository;
		this.chunkJpaRepository = chunkJpaRepository;
	}
	
	@Override
	public KnowledgeDocument saveDocument(TenantId tenantId, String title, String source, String content, Map<String, Object> metadata) {
		return toDocument(documentJpaRepository.save(new KnowledgeDocumentEntity(tenantId.value(), title, source, content, metadata)));
	}
	
	@Override
	public KnowledgeChunk saveChunk(TenantId tenantId, UUID documentId, int chunkIndex, String content) {
		return toChunk(chunkJpaRepository.save(new KnowledgeChunkEntity(tenantId.value(), documentId, chunkIndex, content)));
	}
	
	@Override
	public Page<KnowledgeDocument> findByTenantId(TenantId tenantId, Pageable pageable) {
		return documentJpaRepository.findByTenantId(tenantId.value(), pageable).map(JpaKnowledgeRepositoryAdapter::toDocument);
	}
	
	@Override
	public Optional<KnowledgeDocument> findByTenantIdAndId(TenantId tenantId, UUID documentId) {
		return documentJpaRepository.findByTenantIdAndId(tenantId.value(), documentId).map(JpaKnowledgeRepositoryAdapter::toDocument);
	}
	
	@Override
	public List<KnowledgeChunk> findChunksByTenantIdAndDocumentId(TenantId tenantId, UUID documentId) {
		return chunkJpaRepository.findByTenantIdAndDocumentIdOrderByChunkIndexAsc(tenantId.value(), documentId)
				.stream()
				.map(JpaKnowledgeRepositoryAdapter::toChunk)
				.toList();
	}
	
	@Override
	public void deleteByTenantIdAndId(TenantId tenantId, UUID documentId) {
		chunkJpaRepository.deleteByTenantIdAndDocumentId(tenantId.value(), documentId);
		documentJpaRepository.deleteByTenantIdAndId(tenantId.value(), documentId);
	}
	
	private static KnowledgeDocument toDocument(KnowledgeDocumentEntity entity) {
		return new KnowledgeDocument(
				entity.getId(),
				TenantId.of(entity.getTenantId()),
				entity.getTitle(),
				entity.getSource(),
				entity.getContent(),
				entity.getMetadata(),
				entity.getCreatedAt(),
				entity.getUpdatedAt()
		);
	}
	
	private static KnowledgeChunk toChunk(KnowledgeChunkEntity entity) {
		return new KnowledgeChunk(
				entity.getId(),
				TenantId.of(entity.getTenantId()),
				entity.getDocumentId(),
				entity.getChunkIndex(),
				entity.getContent(),
				entity.getCreatedAt()
		);
	}
}

