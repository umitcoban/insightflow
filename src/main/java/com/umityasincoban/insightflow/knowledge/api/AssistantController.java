package com.umityasincoban.insightflow.knowledge.api;

import com.umityasincoban.insightflow.knowledge.application.AssistantApplicationService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/assistant")
public class AssistantController {
	
	private final AssistantApplicationService assistantApplicationService;
	
	public AssistantController(AssistantApplicationService assistantApplicationService) {
		this.assistantApplicationService = assistantApplicationService;
	}
	
	@PostMapping("/questions")
	public AssistantAnswerResponse ask(@Valid @RequestBody AssistantQuestionRequest request) {
		return AssistantAnswerResponse.from(assistantApplicationService.answer(request.question()));
	}
}

