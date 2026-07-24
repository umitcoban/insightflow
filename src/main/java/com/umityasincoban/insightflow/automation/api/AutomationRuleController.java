package com.umityasincoban.insightflow.automation.api;

import com.umityasincoban.insightflow.automation.application.AutomationRuleApplicationService;
import com.umityasincoban.insightflow.shared.api.PageResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

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
	
	@GetMapping("/{ruleId}")
	public AutomationRuleResponse getRule(@PathVariable UUID ruleId) {
		return AutomationRuleResponse.from(automationRuleApplicationService.getRule(ruleId));
	}
	
	@PatchMapping("/{ruleId}")
	public AutomationRuleResponse updateRule(
			@PathVariable UUID ruleId,
			@RequestBody UpdateAutomationRuleRequest request
	) {
		return AutomationRuleResponse.from(
				automationRuleApplicationService.updateRule(
						ruleId,
						request.name(),
						request.description(),
						request.triggerEventType(),
						request.conditionJson(),
						request.actionJson(),
						request.priority()
				)
		);
	}
	
	@PostMapping("/{ruleId}/activate")
	public AutomationRuleResponse activateRule(@PathVariable UUID ruleId) {
		return AutomationRuleResponse.from(automationRuleApplicationService.activateRule(ruleId));
	}
	
	@PostMapping("/{ruleId}/deactivate")
	public AutomationRuleResponse deactivateRule(@PathVariable UUID ruleId) {
		return AutomationRuleResponse.from(automationRuleApplicationService.deactivateRule(ruleId));
	}
	
	@DeleteMapping("/{ruleId}")
	public ResponseEntity<Void> deleteRule(@PathVariable UUID ruleId) {
		automationRuleApplicationService.deleteRule(ruleId);
		return ResponseEntity.noContent().build();
	}
	
	@PostMapping("/{ruleId}/dry-run")
	public AutomationDryRunResponse dryRun(
			@PathVariable UUID ruleId,
			@RequestBody AutomationDryRunRequest request
	) {
		return new AutomationDryRunResponse(automationRuleApplicationService.dryRun(ruleId, request.payload()));
	}
	
	@PostMapping("/{ruleId}/replay")
	public ResponseEntity<Void> replay(
			@PathVariable UUID ruleId,
			@RequestBody AutomationReplayRequest request
	) {
		automationRuleApplicationService.replay(ruleId, request.sourceEventId(), request.payload());
		return ResponseEntity.accepted().build();
	}
}
