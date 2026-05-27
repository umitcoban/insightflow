package com.umityasincoban.insightflow.outbox.application;

import java.util.UUID;

public class OutboxEventSerializationException extends RuntimeException {
	
	public OutboxEventSerializationException(UUID eventId, Throwable cause) {
		super("Failed to serialize outbox event with id: " + eventId, cause);
	}
}