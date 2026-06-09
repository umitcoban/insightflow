package com.umityasincoban.insightflow.automation.infrastructure.persistence;

import com.umityasincoban.insightflow.automation.domain.AutomationRule;
import com.umityasincoban.insightflow.automation.domain.AutomationRuleId;
import com.umityasincoban.insightflow.tenancy.domain.TenantId;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.UUID;

@Mapper
public interface AutomationRulePersistenceMapper {
	
	@Mapping(target = "id", source = "id")
	@Mapping(target = "tenantId", source = "tenantId")
	AutomationRule toDomain(AutomationRuleEntity entity);
	
	default AutomationRuleId mapAutomationRuleId(UUID value) {
		return value == null ? null : AutomationRuleId.of(value);
	}
	
	default TenantId mapTenantId(UUID value) {
		return value == null ? null : TenantId.of(value);
	}
}
