package com.umityasincoban.insightflow.outbox.application;

import com.umityasincoban.insightflow.outbox.domain.OutboxEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class KafkaOutboxEventPublisher implements OutboxEventPublisher {
	
	private final KafkaTemplate<String, String> kafkaTemplate;
	private final OutboxEventMessageSerializer messageSerializer;
	private final String topicName;
	
	public KafkaOutboxEventPublisher(
			KafkaTemplate<String, String> kafkaTemplate,
			OutboxEventMessageSerializer messageSerializer,
			@Value("${insightflow.kafka.topics.domain-events}") String topicName
	) {
		this.kafkaTemplate = kafkaTemplate;
		this.messageSerializer = messageSerializer;
		this.topicName = topicName;
	}
	
	@Override
	public void publish(OutboxEvent event) {
		String key = event.aggregateId().toString();
		String message = messageSerializer.serialize(event);
		
		try {
			kafkaTemplate.send(topicName, key, message)
					.get(10, TimeUnit.SECONDS);
		} catch (Exception exception) {
			throw new OutboxEventPublishException(event.id(), exception);
		}
	}
}