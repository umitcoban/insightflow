package com.umityasincoban.insightflow.feedback.domain;

import java.util.Objects;
import java.util.UUID;

public record FeedbackId(UUID value) {
	
	public FeedbackId {
		Objects.requireNonNull(value, "Feedback id cannot be null");
	}
	
	public static FeedbackId of(UUID value) {
		return new FeedbackId(value);
	}
}