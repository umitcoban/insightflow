package com.umityasincoban.insightflow.feedback.api;

import com.umityasincoban.insightflow.feedback.application.FeedbackApplicationService;
import com.umityasincoban.insightflow.feedback.domain.FeedbackPriority;
import com.umityasincoban.insightflow.feedback.domain.FeedbackStatus;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import com.umityasincoban.insightflow.feedback.application.FeedbackQuery;
import com.umityasincoban.insightflow.shared.api.PageResponse;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/feedbacks")
public class FeedbackController {
	
	private final FeedbackApplicationService feedbackApplicationService;
	
	public FeedbackController(FeedbackApplicationService feedbackApplicationService) {
		this.feedbackApplicationService = feedbackApplicationService;
	}
	
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public FeedbackResponse createFeedback(@Valid @RequestBody CreateFeedbackRequest request) {
		return FeedbackResponse.from(
				feedbackApplicationService.createFeedback(
						request.customerId(),
						request.source(),
						request.title(),
						request.content(),
						request.priority(),
						request.metadata()
				)
		);
	}
	
	@GetMapping
	public PageResponse<FeedbackResponse> listFeedbacks(
			@RequestParam(required = false) FeedbackStatus status,
			@RequestParam(required = false) FeedbackPriority priority,
			@RequestParam(required = false) Integer page,
			@RequestParam(required = false) Integer size
	) {
		FeedbackQuery query = FeedbackQuery.of(status, priority, page, size);
		
		return PageResponse.from(
				feedbackApplicationService.listFeedbacks(query),
				FeedbackResponse::from
		);
	}
	
	@GetMapping("/{id}")
	public FeedbackResponse getFeedbackById(@PathVariable UUID id) {
		return FeedbackResponse.from(
				feedbackApplicationService.getFeedbackById(id)
		);
	}
}