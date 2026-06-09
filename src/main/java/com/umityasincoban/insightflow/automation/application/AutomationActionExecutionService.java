package com.umityasincoban.insightflow.automation.application;

import com.umityasincoban.insightflow.automation.domain.AutomationActionExecutionRepository;
import com.umityasincoban.insightflow.automation.domain.AutomationExecutionId;
import com.umityasincoban.insightflow.automation.domain.AutomationRuleId;
import com.umityasincoban.insightflow.tenancy.domain.TenantId;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class AutomationActionExecutionService {
	
	private final AutomationActionExecutor automationActionExecutor;
	private final AutomationActionExecutionRepository automationActionExecutionRepository;
	
	public AutomationActionExecutionService(
			AutomationActionExecutor automationActionExecutor,
			AutomationActionExecutionRepository automationActionExecutionRepository
	) {
		this.automationActionExecutor = automationActionExecutor;
		this.automationActionExecutionRepository = automationActionExecutionRepository;
	}
	
	public List<AutomationActionExecutionResult> executeActions(
			TenantId tenantId,
			AutomationRuleId ruleId,
			AutomationExecutionId executionId,
			UUID sourceEventId,
			List<Map<String, Object>> actions
	) {
		return actions.stream()
				.map(action -> executeAction(tenantId, ruleId, executionId, sourceEventId, action))
				.toList();
	}
	
	private AutomationActionExecutionResult executeAction(
			TenantId tenantId,
			AutomationRuleId ruleId,
			AutomationExecutionId executionId,
			UUID sourceEventId,
			Map<String, Object> action
	) {
		AutomationActionExecutionResult result;
		
		try {
			result = automationActionExecutor.execute(tenantId, ruleId, sourceEventId, action);
		} catch (RuntimeException exception) {
			result = AutomationActionExecutionResult.failed(
					resolveActionType(action),
					exception.getMessage() == null ? "Automation action failed" : exception.getMessage()
			);
		}
		
		if (result.success()) {
			automationActionExecutionRepository.saveSuccess(
					tenantId,
					executionId,
					result.actionType(),
					action,
					result.resultPayload()
			);
		} else {
			automationActionExecutionRepository.saveFailed(
					tenantId,
					executionId,
					result.actionType(),
					action,
					result.errorMessage()
			);
		}
		
		return result;
	}
	
	private static String resolveActionType(Map<String, Object> action) {
		if (action == null || action.get("type") == null) {
			return "UNKNOWN";
		}
		
		return action.get("type").toString();
	}
}
