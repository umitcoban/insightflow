package com.umityasincoban.insightflow.search.api;

import com.umityasincoban.insightflow.feedback.domain.FeedbackPriority;
import com.umityasincoban.insightflow.feedback.domain.FeedbackRiskLevel;
import com.umityasincoban.insightflow.feedback.domain.FeedbackSentiment;
import com.umityasincoban.insightflow.feedback.domain.FeedbackSource;
import com.umityasincoban.insightflow.feedback.domain.FeedbackStatus;
import com.umityasincoban.insightflow.search.application.FeedbackSearchQuery;
import com.umityasincoban.insightflow.search.application.FeedbackSearchService;
import com.umityasincoban.insightflow.shared.api.PageResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/feedbacks/search")
public class FeedbackSearchController {
	
	private final FeedbackSearchService feedbackSearchService;
	
	public FeedbackSearchController(FeedbackSearchService feedbackSearchService) {
		this.feedbackSearchService = feedbackSearchService;
	}
	
	@GetMapping
	public PageResponse<FeedbackSearchResponse> search(
			@RequestParam(required = false) String q,
			@RequestParam(required = false) FeedbackStatus status,
			@RequestParam(required = false) FeedbackPriority priority,
			@RequestParam(required = false) FeedbackSentiment sentiment,
			@RequestParam(required = false) FeedbackRiskLevel riskLevel,
			@RequestParam(required = false) String category,
			@RequestParam(required = false) FeedbackSource source,
			@RequestParam(required = false) UUID customerId,
			@RequestParam(required = false) OffsetDateTime from,
			@RequestParam(required = false) OffsetDateTime to,
			@RequestParam(required = false) Integer page,
			@RequestParam(required = false) Integer size
	) {
		PageResponse<FeedbackSearchResponse> response = map(feedbackSearchService.search(FeedbackSearchQuery.of(
				q,
				status,
				priority,
				sentiment,
				riskLevel,
				category,
				source,
				customerId,
				from,
				to,
				page,
				size
		)));
		return response;
	}
	
	private static PageResponse<FeedbackSearchResponse> map(PageResponse<com.umityasincoban.insightflow.search.application.FeedbackSearchDocument> page) {
		return new PageResponse<>(
				page.content().stream().map(FeedbackSearchResponse::from).toList(),
				page.page(),
				page.size(),
				page.totalElements(),
				page.totalPages(),
				page.first(),
				page.last()
		);
	}
}

