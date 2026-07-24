package com.umityasincoban.insightflow.automation.application;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AutomationRulePayloadValidatorTest {
	
	private final AutomationRulePayloadValidator validator = new AutomationRulePayloadValidator();
	
	@Test
	void rejectsUnsupportedActionType() {
		assertThatThrownBy(() -> validator.validate(Map.of(), List.of(Map.of("type", "EMAIL"))))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("Unsupported automation action type");
	}
	
	@Test
	void rejectsUnsupportedConditionOperator() {
		assertThatThrownBy(() -> validator.validate(
				Map.of("all", List.of(Map.of("path", "riskLevel", "op", "startsWith", "value", "HIGH"))),
				List.of(Map.of("type", "LOG"))
		))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("Unsupported automation condition operator");
	}
}

