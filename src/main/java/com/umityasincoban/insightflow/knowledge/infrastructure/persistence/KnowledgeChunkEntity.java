package com.umityasincoban.insightflow.knowledge.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "knowledge_chunks")
public class KnowledgeChunkEntity {
	
	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;
	
	@Column(name = "tenant_id", nullable = false)
	private UUID tenantId;
	
	@Column(name = "document_id", nullable = false)
	private UUID documentId;
	
	@Column(name = "chunk_index", nullable = false)
	private int chunkIndex;
	
	@Column(name = "content", nullable = false, columnDefinition = "text")
	private String content;
	
	@Column(name = "created_at", nullable = false)
	private OffsetDateTime createdAt;
	
	protected KnowledgeChunkEntity() {
	}
	
	public KnowledgeChunkEntity(UUID tenantId, UUID documentId, int chunkIndex, String content) {
		this.tenantId = tenantId;
		this.documentId = documentId;
		this.chunkIndex = chunkIndex;
		this.content = content;
		this.createdAt = OffsetDateTime.now();
	}
	
	public UUID getId() {
		return id;
	}
	
	public UUID getTenantId() {
		return tenantId;
	}
	
	public UUID getDocumentId() {
		return documentId;
	}
	
	public int getChunkIndex() {
		return chunkIndex;
	}
	
	public String getContent() {
		return content;
	}
	
	public OffsetDateTime getCreatedAt() {
		return createdAt;
	}
}

