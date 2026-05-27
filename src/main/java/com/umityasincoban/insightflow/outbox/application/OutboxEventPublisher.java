package com.umityasincoban.insightflow.outbox.application;

import com.umityasincoban.insightflow.outbox.domain.OutboxEvent;

public interface OutboxEventPublisher {
	
	void publish(OutboxEvent event);
}