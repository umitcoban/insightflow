package com.umityasincoban.insightflow.feedback.domain;

import com.umityasincoban.insightflow.customer.domain.CustomerId;
import com.umityasincoban.insightflow.tenancy.domain.TenantId;

import java.time.OffsetDateTime;
import java.util.Objects;

public class Feedback {
	
	private final FeedbackId id;
	private final TenantId tenantId;
	private final CustomerId customerId;
	private final FeedbackSource source;
	private final String title;
	private final String content;
	private final FeedbackStatus status;
	private final FeedbackPriority priority;
	private final FeedbackSentiment sentiment;
	private final String category;
	private final FeedbackRiskLevel riskLevel;
	private final String aiSummary;
	private final String suggestedAction;
	private final OffsetDateTime createdAt;
	private final OffsetDateTime updatedAt;
	
	public Feedback(
			FeedbackId id,
			TenantId tenantId,
			CustomerId customerId,
			FeedbackSource source,
			String title,
			String content,
			FeedbackStatus status,
			FeedbackPriority priority,
			FeedbackSentiment sentiment,
			String category,
			FeedbackRiskLevel riskLevel,
			String aiSummary,
			String suggestedAction,
			OffsetDateTime createdAt,
			OffsetDateTime updatedAt
	) {
		this.id = Objects.requireNonNull(id, "Feedback id cannot be null");
		this.tenantId = Objects.requireNonNull(tenantId, "Tenant id cannot be null");
		this.customerId = customerId;
		this.source = Objects.requireNonNull(source, "Feedback source cannot be null");
		this.title = validateTitle(title);
		this.content = validateContent(content);
		this.status = Objects.requireNonNull(status, "Feedback status cannot be null");
		this.priority = Objects.requireNonNull(priority, "Feedback priority cannot be null");
		this.sentiment = sentiment;
		this.category = category;
		this.riskLevel = riskLevel;
		this.aiSummary = aiSummary;
		this.suggestedAction = suggestedAction;
		this.createdAt = Objects.requireNonNull(createdAt, "Feedback createdAt cannot be null");
		this.updatedAt = Objects.requireNonNull(updatedAt, "Feedback updatedAt cannot be null");
	}
	
	public FeedbackId getId() {
		return id;
	}
	
	public TenantId getTenantId() {
		return tenantId;
	}
	
	public CustomerId getCustomerId() {
		return customerId;
	}
	
	public FeedbackSource getSource() {
		return source;
	}
	
	public String getTitle() {
		return title;
	}
	
	public String getContent() {
		return content;
	}
	
	public FeedbackStatus getStatus() {
		return status;
	}
	
	public FeedbackPriority getPriority() {
		return priority;
	}
	
	public FeedbackSentiment getSentiment() {
		return sentiment;
	}
	
	public String getCategory() {
		return category;
	}
	
	public FeedbackRiskLevel getRiskLevel() {
		return riskLevel;
	}
	
	public String getAiSummary() {
		return aiSummary;
	}
	
	public String getSuggestedAction() {
		return suggestedAction;
	}
	
	public OffsetDateTime getCreatedAt() {
		return createdAt;
	}
	
	public OffsetDateTime getUpdatedAt() {
		return updatedAt;
	}
	
	public boolean isNew() {
		return FeedbackStatus.NEW.equals(status);
	}
	
	public boolean isCritical() {
		return FeedbackPriority.CRITICAL.equals(priority);
	}
	
	public boolean hasAiAnalysis() {
		return sentiment != null || aiSummary != null || suggestedAction != null;
	}
	
	private static String validateTitle(String title) {
		if (title == null || title.isBlank()) {
			throw new IllegalArgumentException("Feedback title cannot be blank");
		}
		
		if (title.length() > 200) {
			throw new IllegalArgumentException("Feedback title cannot be longer than 200 characters");
		}
		
		return title;
	}
	
	private static String validateContent(String content) {
		if (content == null || content.isBlank()) {
			throw new IllegalArgumentException("Feedback content cannot be blank");
		}
		
		return content;
	}
}