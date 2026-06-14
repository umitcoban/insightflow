package com.umityasincoban.insightflow.automation.application;

import java.util.UUID;

public class AutomationRuleNotFoundException extends RuntimeException {
	
	public AutomationRuleNotFoundException(UUID ruleId) {
		super("Automation rule not found: " + ruleId);
	}
}
