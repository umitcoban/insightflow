package com.umityasincoban.insightflow.automation.application;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
public class AutomationConditionEvaluator {
	
	public boolean matches(Map<String, Object> conditionJson, Map<String, Object> payload) {
		if (conditionJson == null || conditionJson.isEmpty()) {
			return true;
		}
		
		if (payload == null) {
			return false;
		}
		
		return conditionJson.entrySet()
				.stream()
				.allMatch(condition -> matchesCondition(condition.getKey(), condition.getValue(), payload));
	}
	
	private static boolean matchesCondition(String path, Object expectedValue, Map<String, Object> payload) {
		Object actualValue = resolvePath(path, payload);
		
		if (actualValue == null) {
			return false;
		}
		
		if (expectedValue instanceof List<?> expectedValues) {
			return expectedValues.stream()
					.anyMatch(expected -> valuesEqual(expected, actualValue));
		}
		
		return valuesEqual(expectedValue, actualValue);
	}
	
	@SuppressWarnings("unchecked")
	private static Object resolvePath(String path, Map<String, Object> payload) {
		if (path == null || path.isBlank()) {
			return null;
		}
		
		Object currentValue = payload;
		
		for (String segment : path.split("\\.")) {
			if (!(currentValue instanceof Map<?, ?> currentMap)) {
				return null;
			}
			
			currentValue = ((Map<String, Object>) currentMap).get(segment);
		}
		
		return currentValue;
	}
	
	private static boolean valuesEqual(Object expectedValue, Object actualValue) {
		if (Objects.equals(expectedValue, actualValue)) {
			return true;
		}
		
		return expectedValue != null
				&& actualValue != null
				&& expectedValue.toString().equals(actualValue.toString());
	}
}
