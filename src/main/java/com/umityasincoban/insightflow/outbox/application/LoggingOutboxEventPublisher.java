package com.umityasincoban.insightflow.outbox.application;

import com.umityasincoban.insightflow.outbox.domain.OutboxEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.context.annotation.Profile;

@Component
@Profile("local-log-publisher")
public class LoggingOutboxEventPublisher implements OutboxEventPublisher {
	
	private static final Logger log = LoggerFactory.getLogger(LoggingOutboxEventPublisher.class);
	
	@Override
	public void publish(OutboxEvent event) {
		log.info(
				"Publishing outbox event id={} tenantId={} aggregateType={} aggregateId={} eventType={} version={} payload={}",
				event.id(),
				event.tenantId() == null ? null : event.tenantId().value(),
				event.aggregateType(),
				event.aggregateId(),
				event.eventType(),
				event.eventVersion(),
				event.payload()
		);
	}
}