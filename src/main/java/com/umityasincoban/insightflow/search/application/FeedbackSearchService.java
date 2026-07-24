package com.umityasincoban.insightflow.search.application;

import com.umityasincoban.insightflow.feedback.application.FeedbackNotFoundException;
import com.umityasincoban.insightflow.feedback.domain.Feedback;
import com.umityasincoban.insightflow.feedback.domain.FeedbackId;
import com.umityasincoban.insightflow.feedback.domain.FeedbackRepository;
import com.umityasincoban.insightflow.shared.api.PageResponse;
import com.umityasincoban.insightflow.shared.tenancy.CurrentTenantProvider;
import com.umityasincoban.insightflow.tenancy.domain.TenantId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class FeedbackSearchService {
	
	private final FeedbackRepository feedbackRepository;
	private final FeedbackSearchPort feedbackSearchPort;
	private final CurrentTenantProvider currentTenantProvider;
	
	public FeedbackSearchService(
			FeedbackRepository feedbackRepository,
			FeedbackSearchPort feedbackSearchPort,
			CurrentTenantProvider currentTenantProvider
	) {
		this.feedbackRepository = feedbackRepository;
		this.feedbackSearchPort = feedbackSearchPort;
		this.currentTenantProvider = currentTenantProvider;
	}
	
	@Transactional(readOnly = true)
	public PageResponse<FeedbackSearchDocument> search(FeedbackSearchQuery query) {
		return feedbackSearchPort.search(currentTenantProvider.getCurrentTenantId(), query);
	}
	
	@Transactional(readOnly = true)
	public void indexFeedback(UUID tenantId, UUID feedbackId) {
		TenantId resolvedTenantId = TenantId.of(tenantId);
		Feedback feedback = feedbackRepository.findByTenantIdAndId(resolvedTenantId, FeedbackId.of(feedbackId))
				.orElseThrow(() -> new FeedbackNotFoundException(feedbackId));
		feedbackSearchPort.index(FeedbackSearchDocument.from(feedback));
	}
}

