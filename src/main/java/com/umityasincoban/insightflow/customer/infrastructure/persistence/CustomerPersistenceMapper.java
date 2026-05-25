package com.umityasincoban.insightflow.customer.infrastructure.persistence;

import com.umityasincoban.insightflow.customer.domain.Customer;
import com.umityasincoban.insightflow.customer.domain.CustomerId;
import com.umityasincoban.insightflow.tenancy.domain.TenantId;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.UUID;

@Mapper
public interface CustomerPersistenceMapper {
	
	@Mapping(target = "id", source = "id")
	@Mapping(target = "tenantId", source = "tenantId")
	Customer toDomain(CustomerEntity entity);
	
	default CustomerId mapCustomerId(UUID value) {
		return value == null ? null : CustomerId.of(value);
	}
	
	default TenantId mapTenantId(UUID value) {
		return value == null ? null : TenantId.of(value);
	}
}