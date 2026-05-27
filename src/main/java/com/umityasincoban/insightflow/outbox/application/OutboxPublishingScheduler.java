package com.umityasincoban.insightflow.outbox.application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class OutboxPublishingScheduler {
	
	private static final Logger log = LoggerFactory.getLogger(OutboxPublishingScheduler.class);
	
	private final OutboxPublishingService outboxPublishingService;
	
	public OutboxPublishingScheduler(OutboxPublishingService outboxPublishingService) {
		this.outboxPublishingService = outboxPublishingService;
	}
	
	@Scheduled(fixedDelayString = "${insightflow.outbox.publisher.fixed-delay-ms:5000}")
	public void publishPendingEvents() {
		int publishedEventCount = outboxPublishingService.publishPendingEvents();
		
		if (publishedEventCount > 0) {
			log.info("Outbox publisher processed {} event(s)", publishedEventCount);
		}
	}
}