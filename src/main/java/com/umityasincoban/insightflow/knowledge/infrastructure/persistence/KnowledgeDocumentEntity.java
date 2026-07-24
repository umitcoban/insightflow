package com.umityasincoban.insightflow.knowledge.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "knowledge_documents")
public class KnowledgeDocumentEntity {
	
	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;
	
	@Column(name = "tenant_id", nullable = false)
	private UUID tenantId;
	
	@Column(name = "title", nullable = false, length = 240)
	private String title;
	
	@Column(name = "source", nullable = false, length = 80)
	private String source;
	
	@Column(name = "content", nullable = false, columnDefinition = "text")
	private String content;
	
	@JdbcTypeCode(SqlTypes.JSON)
	@Column(name = "metadata", nullable = false, columnDefinition = "jsonb")
	private Map<String, Object> metadata;
	
	@Column(name = "created_at", nullable = false)
	private OffsetDateTime createdAt;
	
	@Column(name = "updated_at", nullable = false)
	private OffsetDateTime updatedAt;
	
	protected KnowledgeDocumentEntity() {
	}
	
	public KnowledgeDocumentEntity(UUID tenantId, String title, String source, String content, Map<String, Object> metadata) {
		this.tenantId = tenantId;
		this.title = title;
		this.source = source;
		this.content = content;
		this.metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
		this.createdAt = OffsetDateTime.now();
		this.updatedAt = OffsetDateTime.now();
	}
	
	public UUID getId() {
		return id;
	}
	
	public UUID getTenantId() {
		return tenantId;
	}
	
	public String getTitle() {
		return title;
	}
	
	public String getSource() {
		return source;
	}
	
	public String getContent() {
		return content;
	}
	
	public Map<String, Object> getMetadata() {
		return metadata;
	}
	
	public OffsetDateTime getCreatedAt() {
		return createdAt;
	}
	
	public OffsetDateTime getUpdatedAt() {
		return updatedAt;
	}
}

