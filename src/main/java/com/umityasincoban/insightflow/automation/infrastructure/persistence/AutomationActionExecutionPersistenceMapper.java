package com.umityasincoban.insightflow.automation.infrastructure.persistence;

import com.umityasincoban.insightflow.automation.domain.AutomationActionExecution;
import com.umityasincoban.insightflow.automation.domain.AutomationActionExecutionId;
import com.umityasincoban.insightflow.automation.domain.AutomationExecutionId;
import com.umityasincoban.insightflow.tenancy.domain.TenantId;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.UUID;

@Mapper
public interface AutomationActionExecutionPersistenceMapper {
	
	@Mapping(target = "id", source = "id")
	@Mapping(target = "tenantId", source = "tenantId")
	@Mapping(target = "executionId", source = "executionId")
	AutomationActionExecution toDomain(AutomationActionExecutionEntity entity);
	
	default AutomationActionExecutionId mapAutomationActionExecutionId(UUID value) {
		return value == null ? null : AutomationActionExecutionId.of(value);
	}
	
	default AutomationExecutionId mapAutomationExecutionId(UUID value) {
		return value == null ? null : AutomationExecutionId.of(value);
	}
	
	default TenantId mapTenantId(UUID value) {
		return value == null ? null : TenantId.of(value);
	}
}
