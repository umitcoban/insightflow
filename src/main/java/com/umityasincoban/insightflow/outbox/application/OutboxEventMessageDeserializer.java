package com.umityasincoban.insightflow.outbox.application;

import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Component
public class OutboxEventMessageDeserializer {
	
	private final ObjectMapper objectMapper;
	
	public OutboxEventMessageDeserializer(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}
	
	public OutboxEventMessage deserialize(String value) {
		try {
			return objectMapper.readValue(value, OutboxEventMessage.class);
		} catch (JacksonException exception) {
			throw new OutboxEventDeserializationException(exception);
		}
	}
}