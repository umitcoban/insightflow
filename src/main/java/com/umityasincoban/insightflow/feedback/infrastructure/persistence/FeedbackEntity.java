package com.umityasincoban.insightflow.feedback.infrastructure.persistence;

import com.umityasincoban.insightflow.feedback.domain.FeedbackPriority;
import com.umityasincoban.insightflow.feedback.domain.FeedbackRiskLevel;
import com.umityasincoban.insightflow.feedback.domain.FeedbackSentiment;
import com.umityasincoban.insightflow.feedback.domain.FeedbackSource;
import com.umityasincoban.insightflow.feedback.domain.FeedbackStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "feedbacks")
public class FeedbackEntity {
	
	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;
	
	@Column(name = "tenant_id", nullable = false)
	private UUID tenantId;
	
	@Column(name = "customer_id")
	private UUID customerId;
	
	@Enumerated(EnumType.STRING)
	@Column(name = "source", nullable = false, length = 40)
	private FeedbackSource source;
	
	@Column(name = "title", nullable = false, length = 200)
	private String title;
	
	@Column(name = "content", nullable = false, columnDefinition = "text")
	private String content;
	
	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false, length = 40)
	private FeedbackStatus status;
	
	@Enumerated(EnumType.STRING)
	@Column(name = "priority", nullable = false, length = 40)
	private FeedbackPriority priority;
	
	@Enumerated(EnumType.STRING)
	@Column(name = "sentiment", length = 40)
	private FeedbackSentiment sentiment;
	
	@Column(name = "category", length = 80)
	private String category;
	
	@Enumerated(EnumType.STRING)
	@Column(name = "risk_level", length = 40)
	private FeedbackRiskLevel riskLevel;
	
	@Column(name = "ai_summary", columnDefinition = "text")
	private String aiSummary;
	
	@Column(name = "suggested_action", columnDefinition = "text")
	private String suggestedAction;
	
	@JdbcTypeCode(SqlTypes.JSON)
	@Column(name = "metadata", nullable = false, columnDefinition = "jsonb")
	private Map<String, Object> metadata;
	
	@Column(name = "created_at", nullable = false)
	private OffsetDateTime createdAt;
	
	@Column(name = "updated_at", nullable = false)
	private OffsetDateTime updatedAt;
	
	protected FeedbackEntity() {
	}
	
	public FeedbackEntity(
			UUID tenantId,
			UUID customerId,
			FeedbackSource source,
			String title,
			String content,
			FeedbackStatus status,
			FeedbackPriority priority,
			Map<String, Object> metadata
	) {
		this.tenantId = tenantId;
		this.customerId = customerId;
		this.source = source;
		this.title = title;
		this.content = content;
		this.status = status;
		this.priority = priority;
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
	
	public UUID getCustomerId() {
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
	
	public Map<String, Object> getMetadata() {
		return metadata;
	}
	
	public OffsetDateTime getCreatedAt() {
		return createdAt;
	}
	
	public OffsetDateTime getUpdatedAt() {
		return updatedAt;
	}
	
	public void applyAiAnalysis(
			FeedbackSentiment sentiment,
			String category,
			FeedbackRiskLevel riskLevel,
			String aiSummary,
			String suggestedAction
	) {
		this.sentiment = sentiment;
		this.category = category;
		this.riskLevel = riskLevel;
		this.aiSummary = aiSummary;
		this.suggestedAction = suggestedAction;
		this.updatedAt = OffsetDateTime.now();
	}
}