package com.umityasincoban.insightflow.feedback.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "feedback_notes")
public class FeedbackNoteEntity {
	
	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;
	
	@Column(name = "tenant_id", nullable = false)
	private UUID tenantId;
	
	@Column(name = "feedback_id", nullable = false)
	private UUID feedbackId;
	
	@Column(name = "author", nullable = false, length = 180)
	private String author;
	
	@Column(name = "content", nullable = false, columnDefinition = "text")
	private String content;
	
	@Column(name = "created_at", nullable = false)
	private OffsetDateTime createdAt;
	
	protected FeedbackNoteEntity() {
	}
	
	public FeedbackNoteEntity(UUID tenantId, UUID feedbackId, String author, String content) {
		this.tenantId = tenantId;
		this.feedbackId = feedbackId;
		this.author = author;
		this.content = content;
		this.createdAt = OffsetDateTime.now();
	}
	
	public UUID getId() {
		return id;
	}
	
	public UUID getTenantId() {
		return tenantId;
	}
	
	public UUID getFeedbackId() {
		return feedbackId;
	}
	
	public String getAuthor() {
		return author;
	}
	
	public String getContent() {
		return content;
	}
	
	public OffsetDateTime getCreatedAt() {
		return createdAt;
	}
}

