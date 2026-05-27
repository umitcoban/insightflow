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
	private final OutboxRetryPolicy outboxRetryPolicy;
	
	public OutboxPublishingService(
			OutboxEventRepository outboxEventRepository,
			OutboxEventPublisher outboxEventPublisher,
			OutboxRetryPolicy outboxRetryPolicy
	) {
		this.outboxEventRepository = outboxEventRepository;
		this.outboxEventPublisher = outboxEventPublisher;
		this.outboxRetryPolicy = outboxRetryPolicy;
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
			handlePublishFailure(event, exception);
		}
	}
	
	private void handlePublishFailure(OutboxEvent event, Exception exception) {
		String errorMessage = exception.getMessage() == null
				? exception.getClass().getSimpleName()
				: exception.getMessage();
		
		if (outboxRetryPolicy.shouldRetry(event.retryCount())) {
			outboxEventRepository.markForRetry(
					event.id(),
					errorMessage,
					outboxRetryPolicy.nextRetryAt(event.retryCount())
			);
			return;
		}
		
		outboxEventRepository.markAsFailed(event.id(), errorMessage);
	}
}