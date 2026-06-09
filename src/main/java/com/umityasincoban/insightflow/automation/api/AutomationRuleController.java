package com.umityasincoban.insightflow.automation.api;

import com.umityasincoban.insightflow.automation.application.AutomationRuleApplicationService;
import com.umityasincoban.insightflow.shared.api.PageResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/automation/rules")
public class AutomationRuleController {
	
	private final AutomationRuleApplicationService automationRuleApplicationService;
	
	public AutomationRuleController(AutomationRuleApplicationService automationRuleApplicationService) {
		this.automationRuleApplicationService = automationRuleApplicationService;
	}
	
	@PostMapping
	public ResponseEntity<AutomationRuleResponse> createRule(@Valid @RequestBody CreateAutomationRuleRequest request) {
		AutomationRuleResponse response = AutomationRuleResponse.from(
				automationRuleApplicationService.createRule(
						request.name(),
						request.description(),
						request.triggerEventType(),
						request.conditionJson(),
						request.actionJson(),
						request.priority()
				)
		);
		
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}
	
	@GetMapping
	public PageResponse<AutomationRuleResponse> listRules(
			@RequestParam(required = false) Integer page,
			@RequestParam(required = false) Integer size
	) {
		return PageResponse.from(
				automationRuleApplicationService.listRules(page, size),
				AutomationRuleResponse::from
		);
	}
}
