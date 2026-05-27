package com.umityasincoban.insightflow.outbox.infrastructure.persistence;

import com.umityasincoban.insightflow.outbox.domain.OutboxEvent;
import com.umityasincoban.insightflow.tenancy.domain.TenantId;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.UUID;

@Mapper
public interface OutboxEventPersistenceMapper {
	
	@Mapping(target = "tenantId", source = "tenantId")
	OutboxEvent toDomain(OutboxEventEntity entity);
	
	default TenantId mapTenantId(UUID value) {
		return value == null ? null : TenantId.of(value);
	}
}
