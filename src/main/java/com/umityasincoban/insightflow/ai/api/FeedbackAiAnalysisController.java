package com.umityasincoban.insightflow.ai.api;

import com.umityasincoban.insightflow.ai.application.FeedbackAiEnrichmentService;
import com.umityasincoban.insightflow.shared.tenancy.CurrentTenantProvider;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/feedbacks")
public class FeedbackAiAnalysisController {
	
	private final FeedbackAiEnrichmentService feedbackAiEnrichmentService;
	private final CurrentTenantProvider currentTenantProvider;
	
	public FeedbackAiAnalysisController(
			FeedbackAiEnrichmentService feedbackAiEnrichmentService,
			CurrentTenantProvider currentTenantProvider
	) {
		this.feedbackAiEnrichmentService = feedbackAiEnrichmentService;
		this.currentTenantProvider = currentTenantProvider;
	}
	
	@PostMapping("/{feedbackId}/ai-analysis")
	public ResponseEntity<Void> analyzeFeedback(@PathVariable UUID feedbackId) {
		feedbackAiEnrichmentService.enrichFeedback(
				currentTenantProvider.getCurrentTenantId().value(),
				feedbackId
		);
		
		return ResponseEntity.noContent().build();
	}
}
