package com.umityasincoban.insightflow.feedback.application;

import com.umityasincoban.insightflow.customer.domain.CustomerId;
import com.umityasincoban.insightflow.feedback.domain.Feedback;
import com.umityasincoban.insightflow.feedback.domain.FeedbackId;
import com.umityasincoban.insightflow.feedback.domain.FeedbackPriority;
import com.umityasincoban.insightflow.feedback.domain.FeedbackRepository;
import com.umityasincoban.insightflow.feedback.domain.FeedbackSource;
import com.umityasincoban.insightflow.feedback.domain.FeedbackStatus;
import com.umityasincoban.insightflow.shared.tenancy.CurrentTenantProvider;
import com.umityasincoban.insightflow.tenancy.domain.TenantId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class FeedbackApplicationService {
	
	private final FeedbackRepository feedbackRepository;
	private final CurrentTenantProvider currentTenantProvider;
	
	public FeedbackApplicationService(
			FeedbackRepository feedbackRepository,
			CurrentTenantProvider currentTenantProvider
	) {
		this.feedbackRepository = feedbackRepository;
		this.currentTenantProvider = currentTenantProvider;
	}
	
	@Transactional
	public Feedback createFeedback(
			UUID customerId,
			FeedbackSource source,
			String title,
			String content,
			FeedbackPriority priority,
			Map<String, Object> metadata
	) {
		TenantId tenantId = currentTenantProvider.getCurrentTenantId();
		
		return feedbackRepository.saveNew(
				tenantId,
				customerId == null ? null : CustomerId.of(customerId),
				source,
				title,
				content,
				priority,
				metadata
		);
	}
	
	@Transactional(readOnly = true)
	public Page<Feedback> listFeedbacks(FeedbackQuery query) {
		TenantId tenantId = currentTenantProvider.getCurrentTenantId();
		
		PageRequest pageRequest = PageRequest.of(
				query.page(),
				query.size(),
				Sort.by(Sort.Direction.DESC, "createdAt")
		);
		
		if (query.status() != null) {
			return feedbackRepository.findByTenantIdAndStatus(
					tenantId,
					query.status(),
					pageRequest
			);
		}
		
		if (query.priority() != null) {
			return feedbackRepository.findByTenantIdAndPriority(
					tenantId,
					query.priority(),
					pageRequest
			);
		}
		
		return feedbackRepository.findByTenantId(tenantId, pageRequest);
	}
	
	@Transactional(readOnly = true)
	public Feedback getFeedbackById(UUID feedbackId) {
		TenantId tenantId = currentTenantProvider.getCurrentTenantId();
		
		return feedbackRepository.findByTenantIdAndId(
						tenantId,
						FeedbackId.of(feedbackId)
				)
				.orElseThrow(() -> new FeedbackNotFoundException(feedbackId));
	}
}