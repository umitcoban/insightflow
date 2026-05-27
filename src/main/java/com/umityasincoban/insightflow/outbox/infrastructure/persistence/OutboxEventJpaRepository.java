package com.umityasincoban.insightflow.outbox.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface OutboxEventJpaRepository extends JpaRepository<OutboxEventEntity, UUID> {
	
	@Query(
			value = """
                    select *
                    from outbox_events
                    where status = 'PENDING'
                    order by created_at asc
                    limit :limit
                    for update skip locked
                    """,
			nativeQuery = true
	)
	List<OutboxEventEntity> findPendingEventsForPublishing(int limit);
	
}