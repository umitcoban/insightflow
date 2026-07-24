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
		if (conditionJson.containsKey("all")) {
			return matchesAll(conditionJson.get("all"), payload);
		}
		if (conditionJson.containsKey("any")) {
			return matchesAny(conditionJson.get("any"), payload);
		}
		
		return conditionJson.entrySet()
				.stream()
				.allMatch(condition -> matchesCondition(condition.getKey(), condition.getValue(), payload));
	}
	
	@SuppressWarnings("unchecked")
	private static boolean matchesAll(Object conditions, Map<String, Object> payload) {
		if (!(conditions instanceof List<?> list)) {
			return false;
		}
		return list.stream().allMatch(condition -> condition instanceof Map<?, ?> map && matchesDslCondition((Map<String, Object>) map, payload));
	}
	
	@SuppressWarnings("unchecked")
	private static boolean matchesAny(Object conditions, Map<String, Object> payload) {
		if (!(conditions instanceof List<?> list)) {
			return false;
		}
		return list.stream().anyMatch(condition -> condition instanceof Map<?, ?> map && matchesDslCondition((Map<String, Object>) map, payload));
	}
	
	private static boolean matchesDslCondition(Map<String, Object> condition, Map<String, Object> payload) {
		String path = stringValue(condition.get("path"));
		String operator = stringValue(condition.getOrDefault("op", "eq"));
		Object actualValue = resolvePath(path, payload);
		Object expectedValue = condition.get("value");
		return switch (operator) {
			case "eq" -> actualValue != null && valuesEqual(expectedValue, actualValue);
			case "neq" -> actualValue == null || !valuesEqual(expectedValue, actualValue);
			case "in" -> expectedValue instanceof List<?> list && list.stream().anyMatch(value -> valuesEqual(value, actualValue));
			case "notIn" -> !(expectedValue instanceof List<?> list) || list.stream().noneMatch(value -> valuesEqual(value, actualValue));
			case "contains" -> actualValue != null && expectedValue != null && actualValue.toString().contains(expectedValue.toString());
			case "exists" -> Boolean.TRUE.equals(expectedValue) ? actualValue != null : actualValue == null;
			case "gt" -> compare(actualValue, expectedValue) > 0;
			case "gte" -> compare(actualValue, expectedValue) >= 0;
			case "lt" -> compare(actualValue, expectedValue) < 0;
			case "lte" -> compare(actualValue, expectedValue) <= 0;
			default -> false;
		};
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
	
	private static int compare(Object actualValue, Object expectedValue) {
		if (actualValue == null || expectedValue == null) {
			return Integer.MIN_VALUE;
		}
		Double actualNumber = numberValue(actualValue);
		Double expectedNumber = numberValue(expectedValue);
		if (actualNumber != null && expectedNumber != null) {
			return actualNumber.compareTo(expectedNumber);
		}
		return actualValue.toString().compareTo(expectedValue.toString());
	}
	
	private static Double numberValue(Object value) {
		if (value instanceof Number number) {
			return number.doubleValue();
		}
		try {
			return Double.parseDouble(value.toString());
		} catch (RuntimeException exception) {
			return null;
		}
	}
	
	private static String stringValue(Object value) {
		return value == null ? null : value.toString();
	}
}
