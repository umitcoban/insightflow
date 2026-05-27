package com.umityasincoban.insightflow.feedback.messaging;

import com.umityasincoban.insightflow.feedback.domain.FeedbackEvents;
import com.umityasincoban.insightflow.outbox.application.OutboxEventMessage;
import com.umityasincoban.insightflow.outbox.application.OutboxEventMessageDeserializer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import com.umityasincoban.insightflow.ai.application.FeedbackAiEnrichmentService;

import java.util.UUID;

@Component
public class FeedbackCreatedKafkaConsumer {
	
	private static final Logger log = LoggerFactory.getLogger(FeedbackCreatedKafkaConsumer.class);
	
	private final OutboxEventMessageDeserializer messageDeserializer;
	private final FeedbackAiEnrichmentService feedbackAiEnrichmentService;
	
	public FeedbackCreatedKafkaConsumer(
			OutboxEventMessageDeserializer messageDeserializer,
			FeedbackAiEnrichmentService feedbackAiEnrichmentService
	) {
		this.messageDeserializer = messageDeserializer;
		this.feedbackAiEnrichmentService = feedbackAiEnrichmentService;
	}
	
	@KafkaListener(
			topics = "${insightflow.kafka.topics.domain-events}",
			groupId = "${spring.kafka.consumer.group-id}"
	)
	public void consume(ConsumerRecord<String, String> record) {
		OutboxEventMessage message = messageDeserializer.deserialize(record.value());
		
		if (!FeedbackEvents.FEEDBACK_CREATED.equals(message.eventType())) {
			log.debug(
					"Skipping unsupported event type={} topic={} partition={} offset={}",
					message.eventType(),
					record.topic(),
					record.partition(),
					record.offset()
			);
			return;
		}
		
		log.info(
				"Consumed feedback.created event eventId={} tenantId={} aggregateId={} version={} payload={}",
				message.eventId(),
				message.tenantId(),
				message.aggregateId(),
				message.eventVersion(),
				message.payload()
		);
		
		feedbackAiEnrichmentService.enrichFeedback(
				UUID.fromString(message.tenantId()),
				UUID.fromString(message.aggregateId())
		);
	}
}