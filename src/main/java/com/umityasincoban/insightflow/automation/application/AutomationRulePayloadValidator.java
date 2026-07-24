package com.umityasincoban.insightflow.automation.application;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class AutomationRulePayloadValidator {
	
	private static final Set<String> SUPPORTED_OPERATORS = Set.of(
			"eq", "neq", "in", "notIn", "contains", "exists", "gt", "gte", "lt", "lte"
	);
	private static final Set<String> SUPPORTED_ACTION_TYPES = Set.of("LOG", "WEBHOOK");
	
	public void validate(Map<String, Object> conditionJson, List<Map<String, Object>> actionJson) {
		validateCondition(conditionJson);
		validateActions(actionJson);
	}
	
	@SuppressWarnings("unchecked")
	private void validateCondition(Map<String, Object> conditionJson) {
		if (conditionJson == null) {
			throw new IllegalArgumentException("Automation rule condition is required");
		}
		Object group = conditionJson.get("all");
		if (group == null) {
			group = conditionJson.get("any");
		}
		if (group == null) {
			return;
		}
		if (!(group instanceof List<?> conditions) || conditions.isEmpty()) {
			throw new IllegalArgumentException("Automation rule all/any conditions must be a non-empty array");
		}
		for (Object condition : conditions) {
			if (!(condition instanceof Map<?, ?> conditionMap)) {
				throw new IllegalArgumentException("Automation rule condition item must be an object");
			}
			Map<String, Object> typedCondition = (Map<String, Object>) conditionMap;
			if (isBlank(typedCondition.get("path"))) {
				throw new IllegalArgumentException("Automation rule condition path is required");
			}
			String operator = typedCondition.get("op") == null ? "eq" : typedCondition.get("op").toString();
			if (!SUPPORTED_OPERATORS.contains(operator)) {
				throw new IllegalArgumentException("Unsupported automation condition operator: " + operator);
			}
		}
	}
	
	private void validateActions(List<Map<String, Object>> actionJson) {
		if (actionJson == null || actionJson.isEmpty()) {
			throw new IllegalArgumentException("Automation rule actions cannot be empty");
		}
		for (Map<String, Object> action : actionJson) {
			if (action == null || isBlank(action.get("type"))) {
				throw new IllegalArgumentException("Automation action type is required");
			}
			String type = action.get("type").toString();
			if (!SUPPORTED_ACTION_TYPES.contains(type)) {
				throw new IllegalArgumentException("Unsupported automation action type: " + type);
			}
		}
	}
	
	private static boolean isBlank(Object value) {
		return value == null || value.toString().isBlank();
	}
}

