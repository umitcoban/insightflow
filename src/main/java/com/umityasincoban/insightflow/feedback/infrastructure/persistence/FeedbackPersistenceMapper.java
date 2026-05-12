package com.umityasincoban.insightflow.feedback.infrastructure.persistence;

import com.umityasincoban.insightflow.customer.domain.CustomerId;
import com.umityasincoban.insightflow.feedback.domain.Feedback;
import com.umityasincoban.insightflow.feedback.domain.FeedbackId;
import com.umityasincoban.insightflow.tenancy.domain.TenantId;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.UUID;

@Mapper
public interface FeedbackPersistenceMapper {
	
	@Mapping(target = "id", source = "id")
	@Mapping(target = "tenantId", source = "tenantId")
	@Mapping(target = "customerId", source = "customerId")
	Feedback toDomain(FeedbackEntity entity);
	
	default FeedbackId mapFeedbackId(UUID value) {
		return value == null ? null : FeedbackId.of(value);
	}
	
	default TenantId mapTenantId(UUID value) {
		return value == null ? null : TenantId.of(value);
	}
	
	default CustomerId mapCustomerId(UUID value) {
		return value == null ? null : CustomerId.of(value);
	}
}