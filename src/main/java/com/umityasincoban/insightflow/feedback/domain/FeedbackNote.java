package com.umityasincoban.insightflow.feedback.domain;

import com.umityasincoban.insightflow.tenancy.domain.TenantId;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

public record FeedbackNote(
		UUID id,
		TenantId tenantId,
		FeedbackId feedbackId,
		String author,
		String content,
		OffsetDateTime createdAt
) {
	public FeedbackNote {
		Objects.requireNonNull(id, "Feedback note id cannot be null");
		Objects.requireNonNull(tenantId, "Tenant id cannot be null");
		Objects.requireNonNull(feedbackId, "Feedback id cannot be null");
		if (author == null || author.isBlank()) {
			throw new IllegalArgumentException("Feedback note author cannot be blank");
		}
		if (content == null || content.isBlank()) {
			throw new IllegalArgumentException("Feedback note content cannot be blank");
		}
		Objects.requireNonNull(createdAt, "Feedback note createdAt cannot be null");
	}
}

