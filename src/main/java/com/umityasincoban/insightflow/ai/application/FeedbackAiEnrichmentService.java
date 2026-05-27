package com.umityasincoban.insightflow.ai.application;

import com.umityasincoban.insightflow.ai.domain.FeedbackAiAnalysisResult;
import com.umityasincoban.insightflow.ai.domain.FeedbackAiAnalyzer;
import com.umityasincoban.insightflow.feedback.application.FeedbackNotFoundException;
import com.umityasincoban.insightflow.feedback.domain.Feedback;
import com.umityasincoban.insightflow.feedback.domain.FeedbackId;
import com.umityasincoban.insightflow.feedback.domain.FeedbackRepository;
import com.umityasincoban.insightflow.tenancy.domain.TenantId;
import com.umityasincoban.insightflow.feedback.domain.FeedbackEvents;
import com.umityasincoban.insightflow.outbox.application.OutboxPayloadFactory;
import com.umityasincoban.insightflow.outbox.domain.OutboxEventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class FeedbackAiEnrichmentService {
	
	private final FeedbackRepository feedbackRepository;
	private final FeedbackAiAnalyzer feedbackAiAnalyzer;
	private final OutboxEventRepository outboxEventRepository;
	
	public FeedbackAiEnrichmentService(
			FeedbackRepository feedbackRepository,
			FeedbackAiAnalyzer feedbackAiAnalyzer,
			OutboxEventRepository outboxEventRepository
	) {
		this.feedbackRepository = feedbackRepository;
		this.feedbackAiAnalyzer = feedbackAiAnalyzer;
		this.outboxEventRepository = outboxEventRepository;
	}
	
	@Transactional
	public void enrichFeedback(UUID tenantId, UUID feedbackId) {
		TenantId resolvedTenantId = TenantId.of(tenantId);
		FeedbackId resolvedFeedbackId = FeedbackId.of(feedbackId);
		
		Feedback feedback = feedbackRepository.findByTenantIdAndId(
				resolvedTenantId,
				resolvedFeedbackId
		).orElseThrow(() -> new FeedbackNotFoundException(feedbackId));
		
		FeedbackAiAnalysisResult result = feedbackAiAnalyzer.analyze(
				feedback.getTitle(),
				feedback.getContent()
		);
		
		feedbackRepository.applyAiAnalysis(
				resolvedTenantId,
				resolvedFeedbackId,
				result.sentiment(),
				result.category(),
				result.riskLevel(),
				result.summary(),
				result.suggestedAction()
		);
		
		outboxEventRepository.savePendingEvent(
				resolvedTenantId,
				FeedbackEvents.AGGREGATE_TYPE,
				resolvedFeedbackId.value(),
				FeedbackEvents.AI_ANALYSIS_COMPLETED,
				FeedbackEvents.AI_ANALYSIS_COMPLETED_VERSION,
				OutboxPayloadFactory.feedbackAiAnalysisCompletedPayload(
						resolvedTenantId,
						resolvedFeedbackId,
						result
				)
		);
	}
}