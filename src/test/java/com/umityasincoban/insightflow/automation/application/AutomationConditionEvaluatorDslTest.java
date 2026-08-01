package com.umityasincoban.insightflow.automation.application;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class AutomationConditionEvaluatorDslTest {
	
	private final AutomationConditionEvaluator evaluator = new AutomationConditionEvaluator();
	
	@Test
	void allConditionsMatchWithOperators() {
		boolean matched = evaluator.matches(
				Map.of("all", List.of(
						Map.of("path", "riskLevel", "op", "eq", "value", "HIGH"),
						Map.of("path", "score", "op", "gte", "value", 80),
						Map.of("path", "summary", "op", "contains", "value", "checkout")
				)),
				Map.of("riskLevel", "HIGH", "score", 91, "summary", "checkout failed")
		);
		
		assertThat(matched).isTrue();
	}
	
	@Test
	void anyConditionMatchesWhenOneClauseIsTrue() {
		boolean matched = evaluator.matches(
				Map.of("any", List.of(
						Map.of("path", "priority", "op", "eq", "value", "LOW"),
						Map.of("path", "riskLevel", "op", "in", "value", List.of("HIGH", "CHURN_RISK"))
				)),
				Map.of("priority", "MEDIUM", "riskLevel", "CHURN_RISK")
		);
		
		assertThat(matched).isTrue();
	}
	
	@Test
	void neqDoesNotMatchWhenPayloadFieldIsMissing() {
		Map<String, Object> condition = new HashMap<>();
		condition.put("path", "customerId");
		condition.put("op", "neq");
		condition.put("value", null);
		
		boolean matched = evaluator.matches(
				Map.of("all", List.of(condition)),
				Map.of("sentiment", "NEGATIVE")
		);
		
		assertThat(matched).isFalse();
	}
	
	@Test
	void neqMatchesWhenPayloadFieldExistsAndDiffersFromExpectedValue() {
		Map<String, Object> condition = new HashMap<>();
		condition.put("path", "customerId");
		condition.put("op", "neq");
		condition.put("value", null);
		
		boolean matched = evaluator.matches(
				Map.of("all", List.of(condition)),
				Map.of("customerId", "6a8d8705-7843-4620-b0ec-d71adf494aa7")
		);
		
		assertThat(matched).isTrue();
	}
	
	@Test
	void existsMatchesPresenceChecksExplicitly() {
		boolean matched = evaluator.matches(
				Map.of("all", List.of(
						Map.of("path", "customerId", "op", "exists", "value", true)
				)),
				Map.of("customerId", "6a8d8705-7843-4620-b0ec-d71adf494aa7")
		);
		
		assertThat(matched).isTrue();
	}
}
