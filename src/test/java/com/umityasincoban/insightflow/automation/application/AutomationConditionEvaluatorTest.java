package com.umityasincoban.insightflow.automation.application;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class AutomationConditionEvaluatorTest {
	
	private final AutomationConditionEvaluator evaluator = new AutomationConditionEvaluator();
	
	@Test
	void emptyConditionMatches() {
		boolean result = evaluator.matches(
				Map.of(),
				Map.of("sentiment", "NEGATIVE")
		);
		
		assertThat(result).isTrue();
	}
	
	@Test
	void exactScalarMatches() {
		boolean result = evaluator.matches(
				Map.of("sentiment", "NEGATIVE"),
				Map.of("sentiment", "NEGATIVE")
		);
		
		assertThat(result).isTrue();
	}
	
	@Test
	void exactScalarMismatchDoesNotMatch() {
		boolean result = evaluator.matches(
				Map.of("sentiment", "NEGATIVE"),
				Map.of("sentiment", "POSITIVE")
		);
		
		assertThat(result).isFalse();
	}
	
	@Test
	void listMatchesWhenActualValueIsContained() {
		boolean result = evaluator.matches(
				Map.of("priority", List.of("HIGH", "CRITICAL")),
				Map.of("priority", "CRITICAL")
		);
		
		assertThat(result).isTrue();
	}
	
	@Test
	void listMismatchDoesNotMatch() {
		boolean result = evaluator.matches(
				Map.of("priority", List.of("HIGH", "CRITICAL")),
				Map.of("priority", "LOW")
		);
		
		assertThat(result).isFalse();
	}
	
	@Test
	void missingPayloadFieldDoesNotMatch() {
		boolean result = evaluator.matches(
				Map.of("riskLevel", "CHURN_RISK"),
				Map.of("sentiment", "NEGATIVE")
		);
		
		assertThat(result).isFalse();
	}
	
	@Test
	void multipleConditionsUseAndSemantics() {
		boolean result = evaluator.matches(
				Map.of(
						"sentiment", "NEGATIVE",
						"riskLevel", "CHURN_RISK"
				),
				Map.of(
						"sentiment", "NEGATIVE",
						"riskLevel", "LOW"
				)
		);
		
		assertThat(result).isFalse();
	}
	
	@Test
	void dotNotationMatchesNestedPayload() {
		boolean result = evaluator.matches(
				Map.of("analysis.sentiment", "NEGATIVE"),
				Map.of("analysis", Map.of("sentiment", "NEGATIVE"))
		);
		
		assertThat(result).isTrue();
	}
}
