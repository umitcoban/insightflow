package com.umityasincoban.insightflow.automation.api;

import java.util.Map;

public record AutomationDryRunRequest(
		Map<String, Object> payload
) {
}

