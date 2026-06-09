package com.umityasincoban.insightflow.automation.application;

import com.umityasincoban.insightflow.automation.domain.AutomationRuleId;
import com.umityasincoban.insightflow.tenancy.domain.TenantId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Component
public class AutomationLogActionExecutor implements AutomationActionExecutor {
	
	private static final Logger log = LoggerFactory.getLogger(AutomationLogActionExecutor.class);
	private static final String LOG_ACTION_TYPE = "LOG";
	
	@Override
	public AutomationActionExecutionResult execute(
			TenantId tenantId,
			AutomationRuleId ruleId,
			UUID sourceEventId,
			Map<String, Object> actionJson
	) {
		String actionType = resolveActionType(actionJson);
		
		if (!LOG_ACTION_TYPE.equals(actionType)) {
			return AutomationActionExecutionResult.failed(
					actionType,
					"Unsupported automation action type: " + actionType
			);
		}
		
		String message = resolveMessage(actionJson);
		
		log.info(
				"Automation LOG action tenantId={} ruleId={} sourceEventId={} message={}",
				tenantId.value(),
				ruleId.value(),
				sourceEventId,
				message
		);
		
		return AutomationActionExecutionResult.success(LOG_ACTION_TYPE, Map.of("logged", true));
	}
	
	private static String resolveActionType(Map<String, Object> actionJson) {
		if (actionJson == null) {
			return "UNKNOWN";
		}
		
		Object value = actionJson.get("type");
		
		if (value == null || value.toString().isBlank()) {
			return "UNKNOWN";
		}
		
		return value.toString();
	}
	
	private static String resolveMessage(Map<String, Object> actionJson) {
		Object value = actionJson.get("message");
		
		if (value == null || value.toString().isBlank()) {
			return "Automation rule matched";
		}
		
		return value.toString();
	}
}
