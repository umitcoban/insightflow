package com.umityasincoban.insightflow.feedback.api;

import com.umityasincoban.insightflow.feedback.domain.FeedbackNote;

import java.time.OffsetDateTime;
import java.util.UUID;

public record FeedbackNoteResponse(
		UUID id,
		UUID tenantId,
		UUID feedbackId,
		String author,
		String content,
		OffsetDateTime createdAt
) {
	
	public static FeedbackNoteResponse from(FeedbackNote note) {
		return new FeedbackNoteResponse(
				note.id(),
				note.tenantId().value(),
				note.feedbackId().value(),
				note.author(),
				note.content(),
				note.createdAt()
		);
	}
}

