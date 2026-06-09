package com.umityasincoban.insightflow.automation.infrastructure.messaging;

import com.umityasincoban.insightflow.automation.application.AutomationRuleEvaluationService;
import com.umityasincoban.insightflow.outbox.application.OutboxEventDeserializationException;
import com.umityasincoban.insightflow.outbox.application.OutboxEventMessage;
import com.umityasincoban.insightflow.outbox.application.OutboxEventMessageDeserializer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class AutomationDomainEventConsumer {
	
	private static final Logger log = LoggerFactory.getLogger(AutomationDomainEventConsumer.class);
	
	private final OutboxEventMessageDeserializer messageDeserializer;
	private final AutomationRuleEvaluationService automationRuleEvaluationService;
	
	public AutomationDomainEventConsumer(
			OutboxEventMessageDeserializer messageDeserializer,
			AutomationRuleEvaluationService automationRuleEvaluationService
	) {
		this.messageDeserializer = messageDeserializer;
		this.automationRuleEvaluationService = automationRuleEvaluationService;
	}
	
	@KafkaListener(
			topics = "${insightflow.kafka.topics.domain-events}",
			groupId = "${insightflow.kafka.consumer-groups.automation}"
	)
	public void consume(ConsumerRecord<String, String> record) {
		try {
			OutboxEventMessage message = messageDeserializer.deserialize(record.value());
			automationRuleEvaluationService.evaluate(message);
		} catch (OutboxEventDeserializationException exception) {
			log.warn(
					"Skipping malformed automation domain event topic={} partition={} offset={}",
					record.topic(),
					record.partition(),
					record.offset(),
					exception
			);
		} catch (RuntimeException exception) {
			log.error(
					"Automation domain event processing failed topic={} partition={} offset={}",
					record.topic(),
					record.partition(),
					record.offset(),
					exception
			);
		}
	}
}
