package com.umityasincoban.insightflow.automation.api;

import com.umityasincoban.insightflow.automation.application.AutomationExecutionApplicationService;
import com.umityasincoban.insightflow.shared.api.PageResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
}
