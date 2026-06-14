package com.umityasincoban.insightflow.automation.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AutomationActionExecutionJpaRepository extends JpaRepository<AutomationActionExecutionEntity, UUID> {
	
	List<AutomationActionExecutionEntity> findByTenantIdAndExecutionIdOrderByCreatedAtAsc(UUID tenantId, UUID executionId);
	
	Optional<AutomationActionExecutionEntity> findByTenantIdAndId(UUID tenantId, UUID id);
	
	List<AutomationActionExecutionEntity> findByExecutionId(UUID executionId);
	
	@Query(
			value = """
                select *
                from automation_action_executions
                where action_type = 'WEBHOOK'
                  and (
                    (status = 'RETRY_SCHEDULED' and next_retry_at <= :now and attempt_count < max_attempts)
                    or (status = 'IN_PROGRESS' and last_attempt_at <= :staleBefore)
                  )
                  and coalesce((request_payload ->> 'retryEligible')::boolean, false) = true
                order by coalesce(next_retry_at, last_attempt_at), created_at asc
                limit :limit
                for update skip locked
                """,
			nativeQuery = true
	)
	List<AutomationActionExecutionEntity> findDueWebhookRetriesForUpdate(
			int limit,
			OffsetDateTime now,
			OffsetDateTime staleBefore
	);
}
