package com.umityasincoban.insightflow.outbox.infrastructure.persistence;

import com.umityasincoban.insightflow.outbox.domain.OutboxEventStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface OutboxEventJpaRepository extends JpaRepository<OutboxEventEntity, UUID> {
	
	List<OutboxEventEntity> findTop100ByStatusOrderByCreatedAtAsc(OutboxEventStatus status);
}