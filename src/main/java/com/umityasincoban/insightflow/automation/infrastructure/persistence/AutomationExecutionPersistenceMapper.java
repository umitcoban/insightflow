package com.umityasincoban.insightflow.automation.infrastructure.persistence;

import com.umityasincoban.insightflow.automation.domain.AutomationExecution;
import com.umityasincoban.insightflow.automation.domain.AutomationExecutionId;
import com.umityasincoban.insightflow.automation.domain.AutomationRuleId;
import com.umityasincoban.insightflow.tenancy.domain.TenantId;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.UUID;

@Mapper
public interface AutomationExecutionPersistenceMapper {
	
	@Mapping(target = "id", source = "id")
	@Mapping(target = "tenantId", source = "tenantId")
	@Mapping(target = "ruleId", source = "ruleId")
	AutomationExecution toDomain(AutomationExecutionEntity entity);
	
	default AutomationExecutionId mapAutomationExecutionId(UUID value) {
		return value == null ? null : AutomationExecutionId.of(value);
	}
	
	default AutomationRuleId mapAutomationRuleId(UUID value) {
		return value == null ? null : AutomationRuleId.of(value);
	}
	
	default TenantId mapTenantId(UUID value) {
		return value == null ? null : TenantId.of(value);
	}
}
