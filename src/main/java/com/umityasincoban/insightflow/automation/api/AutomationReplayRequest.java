package com.umityasincoban.insightflow.automation.api;

import java.util.Map;

public record AutomationReplayRequest(
		String sourceEventId,
		Map<String, Object> payload
) {
}

