package com.umityasincoban.insightflow.automation.application;

import java.util.UUID;

public class AutomationRuleDeletionNotAllowedException extends RuntimeException {
	
	public AutomationRuleDeletionNotAllowedException(UUID ruleId) {
		super("Automation rule " + ruleId + " has execution history and cannot be deleted. Deactivate it instead.");
	}
}
