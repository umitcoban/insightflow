package com.umityasincoban.insightflow.automation.api;

import com.umityasincoban.insightflow.automation.application.AutomationExecutionApplicationService;
import com.umityasincoban.insightflow.shared.api.PageResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/automation/executions")
public class AutomationExecutionController {
	
	private final AutomationExecutionApplicationService automationExecutionApplicationService;
	
	public AutomationExecutionController(AutomationExecutionApplicationService automationExecutionApplicationService) {
		this.automationExecutionApplicationService = automationExecutionApplicationService;
	}
	
	@GetMapping
	public PageResponse<AutomationExecutionResponse> listExecutions(
			@RequestParam(required = false) Integer page,
			@RequestParam(required = false) Integer size
	) {
		return PageResponse.from(
				automationExecutionApplicationService.listExecutions(page, size),
				AutomationExecutionResponse::from
		);
	}
	
	@GetMapping("/{executionId}")
	public AutomationExecutionResponse getExecution(@PathVariable UUID executionId) {
		return AutomationExecutionResponse.from(automationExecutionApplicationService.getExecution(executionId));
	}
	
	@GetMapping("/{executionId}/actions")
	public List<AutomationActionExecutionResponse> listActionExecutions(@PathVariable UUID executionId) {
		return automationExecutionApplicationService.listActionExecutions(executionId)
				.stream()
				.map(AutomationActionExecutionResponse::from)
				.toList();
	}
}
