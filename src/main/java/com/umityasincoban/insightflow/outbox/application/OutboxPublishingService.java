package com.umityasincoban.insightflow.outbox.application;

import com.umityasincoban.insightflow.outbox.domain.OutboxEvent;
import com.umityasincoban.insightflow.outbox.domain.OutboxEventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class OutboxPublishingService {
	
	private static final int DEFAULT_BATCH_SIZE = 100;
	
	private final OutboxEventRepository outboxEventRepository;
	private final OutboxEventPublisher outboxEventPublisher;
	
	public OutboxPublishingService(
			OutboxEventRepository outboxEventRepository,
			OutboxEventPublisher outboxEventPublisher
	) {
		this.outboxEventRepository = outboxEventRepository;
		this.outboxEventPublisher = outboxEventPublisher;
	}
	
	@Transactional
	public int publishPendingEvents() {
		List<OutboxEvent> pendingEvents = outboxEventRepository.findPendingEvents(DEFAULT_BATCH_SIZE);
		
		for (OutboxEvent event : pendingEvents) {
			publishSingleEvent(event);
		}
		
		return pendingEvents.size();
	}
	
	private void publishSingleEvent(OutboxEvent event) {
		try {
			outboxEventPublisher.publish(event);
			outboxEventRepository.markAsPublished(event.id());
		} catch (Exception exception) {
			outboxEventRepository.markAsFailed(event.id(), exception.getMessage());
		}
	}
}