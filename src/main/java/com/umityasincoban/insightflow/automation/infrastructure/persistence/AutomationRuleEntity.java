package com.umityasincoban.insightflow.automation.infrastructure.persistence;

import com.umityasincoban.insightflow.automation.domain.AutomationRuleStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "automation_rules")
public class AutomationRuleEntity {
	
	@Id
	private UUID id;
	
	@Column(name = "tenant_id", nullable = false)
	private UUID tenantId;
	
	@Column(name = "name", nullable = false, length = 160)
	private String name;
	
	@Column(name = "description", columnDefinition = "text")
	private String description;
	
	@Column(name = "trigger_event_type", nullable = false, length = 160)
	private String triggerEventType;
	
	@JdbcTypeCode(SqlTypes.JSON)
	@Column(name = "condition_json", nullable = false, columnDefinition = "jsonb")
	private Map<String, Object> conditionJson;
	
	@JdbcTypeCode(SqlTypes.JSON)
	@Column(name = "action_json", nullable = false, columnDefinition = "jsonb")
	private List<Map<String, Object>> actionJson;
	
	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false, length = 40)
	private AutomationRuleStatus status;
	
	@Column(name = "priority", nullable = false)
	private int priority;
	
	@Column(name = "created_at", nullable = false)
	private OffsetDateTime createdAt;
	
	@Column(name = "updated_at", nullable = false)
	private OffsetDateTime updatedAt;
	
	protected AutomationRuleEntity() {
	}
	
	public AutomationRuleEntity(
			UUID tenantId,
			String name,
			String description,
			String triggerEventType,
			Map<String, Object> conditionJson,
			List<Map<String, Object>> actionJson,
			int priority
	) {
		this.id = UUID.randomUUID();
		this.tenantId = tenantId;
		this.name = name;
		this.description = description;
		this.triggerEventType = triggerEventType;
		this.conditionJson = conditionJson == null ? Map.of() : Map.copyOf(conditionJson);
		this.actionJson = actionJson == null ? List.of() : List.copyOf(actionJson);
		this.status = AutomationRuleStatus.ACTIVE;
		this.priority = priority;
		this.createdAt = OffsetDateTime.now();
		this.updatedAt = OffsetDateTime.now();
	}
	
	public UUID getId() {
		return id;
	}
	
	public UUID getTenantId() {
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
	
	public void deactivate() {
		this.status = AutomationRuleStatus.INACTIVE;
		this.updatedAt = OffsetDateTime.now();
	}
	
	public void activate() {
		this.status = AutomationRuleStatus.ACTIVE;
		this.updatedAt = OffsetDateTime.now();
	}
}