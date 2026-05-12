package com.umityasincoban.insightflow.tenancy.infrastructure.persistence;

import com.umityasincoban.insightflow.tenancy.domain.Tenant;
import com.umityasincoban.insightflow.tenancy.domain.TenantId;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.UUID;

@Mapper
public interface TenantPersistenceMapper {
	@Mapping(target = "id", source = "id")
	Tenant toDomain(TenantEntity entity);
	
	default TenantId map(UUID value) {
		return TenantId.of(value);
	}
}
