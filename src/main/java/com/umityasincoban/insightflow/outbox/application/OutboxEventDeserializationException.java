package com.umityasincoban.insightflow.outbox.application;

public class OutboxEventDeserializationException extends RuntimeException {
	
	public OutboxEventDeserializationException(Throwable cause) {
		super("Failed to deserialize outbox event message", cause);
	}
}