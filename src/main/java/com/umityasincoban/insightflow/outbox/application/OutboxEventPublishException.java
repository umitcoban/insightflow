package com.umityasincoban.insightflow.outbox.application;

import java.util.UUID;

public class OutboxEventPublishException extends RuntimeException {
	
	public OutboxEventPublishException(UUID eventId, Throwable cause) {
		super("Failed to publish outbox event with id: " + eventId, cause);
	}
}