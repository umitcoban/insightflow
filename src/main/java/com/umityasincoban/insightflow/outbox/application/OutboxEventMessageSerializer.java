package com.umityasincoban.insightflow.outbox.application;

import com.umityasincoban.insightflow.outbox.domain.OutboxEvent;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class OutboxEventMessageSerializer {
	
	private final ObjectMapper objectMapper;
	
	public OutboxEventMessageSerializer(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}
	
	public String serialize(OutboxEvent event) {
		Map<String, Object> message = new LinkedHashMap<>();
		
		message.put("eventId", event.id().toString());
		message.put("tenantId", event.tenantId() == null ? null : event.tenantId().value().toString());
		message.put("aggregateType", event.aggregateType());
		message.put("aggregateId", event.aggregateId().toString());
		message.put("eventType", event.eventType());
		message.put("eventVersion", event.eventVersion());
		message.put("payload", event.payload());
		message.put("createdAt", event.createdAt().toString());
		
		try {
			return objectMapper.writeValueAsString(message);
		} catch (JacksonException exception) {
			throw new OutboxEventSerializationException(event.id(), exception);
		}
	}
}