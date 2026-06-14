package com.umityasincoban.insightflow.automation.domain;

import java.util.UUID;

public class DuplicateAutomationExecutionException extends RuntimeException {
	
	private final UUID tenantId;
	private final UUID ruleId;
	private final UUID sourceEventId;
	
	public DuplicateAutomationExecutionException(
			UUID tenantId,
			UUID ruleId,
			UUID sourceEventId,
			Throwable cause
	) {
		super("Duplicate automation execution ruleId=%s sourceEventId=%s".formatted(ruleId, sourceEventId), cause);
		this.tenantId = tenantId;
		this.ruleId = ruleId;
		this.sourceEventId = sourceEventId;
	}
	
	public UUID getTenantId() {
		return tenantId;
	}
	
	public UUID getRuleId() {
		return ruleId;
	}
	
	public UUID getSourceEventId() {
		return sourceEventId;
	}
}
