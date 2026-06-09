package com.umityasincoban.insightflow.automation.domain;

import com.umityasincoban.insightflow.tenancy.domain.TenantId;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class AutomationRule {
	
	private final AutomationRuleId id;
	private final TenantId tenantId;
	private final String name;
	private final String description;
	private final String triggerEventType;
	private final Map<String, Object> conditionJson;
	private final List<Map<String, Object>> actionJson;
	private final AutomationRuleStatus status;
	private final int priority;
	private final OffsetDateTime createdAt;
	private final OffsetDateTime updatedAt;
	
	public AutomationRule(
			AutomationRuleId id,
			TenantId tenantId,
			String name,
			String description,
			String triggerEventType,
			Map<String, Object> conditionJson,
			List<Map<String, Object>> actionJson,
			AutomationRuleStatus status,
			int priority,
			OffsetDateTime createdAt,
			OffsetDateTime updatedAt
	) {
		this.id = Objects.requireNonNull(id, "Automation rule id cannot be null");
		this.tenantId = Objects.requireNonNull(tenantId, "Tenant id cannot be null");
		this.name = validateName(name);
		this.description = normalizeOptional(description);
		this.triggerEventType = validateTriggerEventType(triggerEventType);
		this.conditionJson = Map.copyOf(Objects.requireNonNull(conditionJson, "Automation rule condition cannot be null"));
		this.actionJson = List.copyOf(Objects.requireNonNull(actionJson, "Automation rule actions cannot be null"));
		this.status = Objects.requireNonNull(status, "Automation rule status cannot be null");
		this.priority = priority;
		this.createdAt = Objects.requireNonNull(createdAt, "Automation rule createdAt cannot be null");
		this.updatedAt = Objects.requireNonNull(updatedAt, "Automation rule updatedAt cannot be null");
	}
	
	public AutomationRuleId getId() {
		return id;
	}
	
	public TenantId getTenantId() {
		return tenantId;
	}
	
	public String getName() {
		return name;
	}
	
	public String getDescription() {
		return description;
	}
	
	public String getTriggerEventType() {
		return triggerEventType;
	}
	
	public Map<String, Object> getConditionJson() {
		return conditionJson;
	}
	
	public List<Map<String, Object>> getActionJson() {
		return actionJson;
	}
	
	public AutomationRuleStatus getStatus() {
		return status;
	}
	
	public int getPriority() {
		return priority;
	}
	
	public OffsetDateTime getCreatedAt() {
		return createdAt;
	}
	
	public OffsetDateTime getUpdatedAt() {
		return updatedAt;
	}
	
	public boolean isActive() {
		return AutomationRuleStatus.ACTIVE.equals(status);
	}
	
	private static String validateName(String value) {
		if (value == null || value.isBlank()) {
			throw new IllegalArgumentException("Automation rule name cannot be blank");
		}
		
		if (value.length() > 160) {
			throw new IllegalArgumentException("Automation rule name cannot be longer than 160 characters");
		}
		
		return value.strip();
	}
	
	private static String validateTriggerEventType(String value) {
		if (value == null || value.isBlank()) {
			throw new IllegalArgumentException("Automation rule trigger event type cannot be blank");
		}
		
		if (value.length() > 160) {
			throw new IllegalArgumentException("Automation rule trigger event type cannot be longer than 160 characters");
		}
		
		return value.strip();
	}
	
	private static String normalizeOptional(String value) {
		if (value == null || value.isBlank()) {
			return null;
		}
		
		return value.strip();
	}
}
