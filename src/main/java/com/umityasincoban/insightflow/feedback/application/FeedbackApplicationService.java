package com.umityasincoban.insightflow.feedback.application;

import com.umityasincoban.insightflow.customer.domain.CustomerId;
import com.umityasincoban.insightflow.feedback.domain.Feedback;
import com.umityasincoban.insightflow.feedback.domain.FeedbackId;
import com.umityasincoban.insightflow.feedback.domain.FeedbackPriority;
import com.umityasincoban.insightflow.feedback.domain.FeedbackRepository;
import com.umityasincoban.insightflow.feedback.domain.FeedbackSource;
import com.umityasincoban.insightflow.customer.application.CustomerNotFoundException;
import com.umityasincoban.insightflow.customer.domain.CustomerRepository;
import com.umityasincoban.insightflow.shared.tenancy.CurrentTenantProvider;
import com.umityasincoban.insightflow.tenancy.domain.TenantId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.Map;
import java.util.UUID;

@Service
public class FeedbackApplicationService {
	
	private final FeedbackRepository feedbackRepository;
	private final CurrentTenantProvider currentTenantProvider;
	private final CustomerRepository customerRepository;
	
	public FeedbackApplicationService(
			FeedbackRepository feedbackRepository,
			CustomerRepository customerRepository,
			CurrentTenantProvider currentTenantProvider
	) {
		this.feedbackRepository = feedbackRepository;
		this.customerRepository = customerRepository;
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
		
		CustomerId resolvedCustomerId = resolveCustomerId(tenantId, customerId);
		
		return feedbackRepository.saveNew(
				tenantId,
				resolvedCustomerId,
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
	
	private CustomerId resolveCustomerId(TenantId tenantId, UUID customerId) {
		if (customerId == null) {
			return null;
		}
		
		CustomerId resolvedCustomerId = CustomerId.of(customerId);
		
		boolean customerExistsForTenant = customerRepository.existsByTenantIdAndId(
				tenantId,
				resolvedCustomerId
		);
		
		if (!customerExistsForTenant) {
			throw new CustomerNotFoundException(customerId);
		}
		
		return resolvedCustomerId;
	}
}