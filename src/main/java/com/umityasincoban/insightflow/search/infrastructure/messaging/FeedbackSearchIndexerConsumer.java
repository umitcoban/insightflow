package com.umityasincoban.insightflow.search.infrastructure.messaging;

import com.umityasincoban.insightflow.feedback.domain.FeedbackEvents;
import com.umityasincoban.insightflow.outbox.application.OutboxEventDeserializationException;
import com.umityasincoban.insightflow.outbox.application.OutboxEventMessage;
import com.umityasincoban.insightflow.outbox.application.OutboxEventMessageDeserializer;
import com.umityasincoban.insightflow.search.application.FeedbackSearchService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class FeedbackSearchIndexerConsumer {
	
	private static final Logger log = LoggerFactory.getLogger(FeedbackSearchIndexerConsumer.class);
	
	private final OutboxEventMessageDeserializer messageDeserializer;
	private final FeedbackSearchService feedbackSearchService;
	
	public FeedbackSearchIndexerConsumer(
			OutboxEventMessageDeserializer messageDeserializer,
			FeedbackSearchService feedbackSearchService
	) {
		this.messageDeserializer = messageDeserializer;
		this.feedbackSearchService = feedbackSearchService;
	}
	
	@KafkaListener(
			topics = "${insightflow.kafka.topics.domain-events}",
			groupId = "${insightflow.kafka.consumer-groups.search-indexer}"
	)
	public void consume(ConsumerRecord<String, String> record) {
		try {
			OutboxEventMessage message = messageDeserializer.deserialize(record.value());
			if (!FeedbackEvents.FEEDBACK_CREATED.equals(message.eventType())
					&& !FeedbackEvents.AI_ANALYSIS_COMPLETED.equals(message.eventType())
					&& !FeedbackEvents.FEEDBACK_UPDATED.equals(message.eventType())) {
				return;
			}
			feedbackSearchService.indexFeedback(UUID.fromString(message.tenantId()), UUID.fromString(message.aggregateId()));
		} catch (OutboxEventDeserializationException exception) {
			log.warn("Skipping malformed search index event topic={} partition={} offset={}", record.topic(), record.partition(), record.offset(), exception);
		} catch (RuntimeException exception) {
			log.error("Failed to index feedback search document topic={} partition={} offset={}", record.topic(), record.partition(), record.offset(), exception);
		}
	}
}

