package com.umityasincoban.insightflow.feedback.application;

import com.umityasincoban.insightflow.customer.domain.CustomerId;
import com.umityasincoban.insightflow.feedback.domain.Feedback;
import com.umityasincoban.insightflow.feedback.domain.FeedbackId;
import com.umityasincoban.insightflow.feedback.domain.FeedbackPriority;
import com.umityasincoban.insightflow.feedback.domain.FeedbackNote;
import com.umityasincoban.insightflow.feedback.domain.FeedbackRepository;
import com.umityasincoban.insightflow.feedback.domain.FeedbackSource;
import com.umityasincoban.insightflow.feedback.domain.FeedbackStatus;
import com.umityasincoban.insightflow.shared.security.CurrentUserProvider;
import com.umityasincoban.insightflow.customer.application.CustomerNotFoundException;
import com.umityasincoban.insightflow.customer.domain.CustomerRepository;
import com.umityasincoban.insightflow.shared.tenancy.CurrentTenantProvider;
import com.umityasincoban.insightflow.tenancy.domain.TenantId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import com.umityasincoban.insightflow.feedback.domain.FeedbackEvents;
import com.umityasincoban.insightflow.outbox.application.OutboxPayloadFactory;
import com.umityasincoban.insightflow.outbox.domain.OutboxEventRepository;

import java.util.Map;
import java.util.List;
import java.util.UUID;

@Service
public class FeedbackApplicationService {
	
	private final FeedbackRepository feedbackRepository;
	private final CurrentTenantProvider currentTenantProvider;
	private final CustomerRepository customerRepository;
	private final OutboxEventRepository outboxEventRepository;
	private final CurrentUserProvider currentUserProvider;
	
	public FeedbackApplicationService(
			FeedbackRepository feedbackRepository,
			CustomerRepository customerRepository,
			CurrentTenantProvider currentTenantProvider,
			OutboxEventRepository outboxEventRepository,
			CurrentUserProvider currentUserProvider
	) {
		this.feedbackRepository = feedbackRepository;
		this.customerRepository = customerRepository;
		this.currentTenantProvider = currentTenantProvider;
		this.outboxEventRepository = outboxEventRepository;
		this.currentUserProvider = currentUserProvider;
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
		
		Feedback feedback = feedbackRepository.saveNew(
				tenantId,
				resolvedCustomerId,
				source,
				title,
				content,
				priority,
				metadata
		);
		
		outboxEventRepository.savePendingEvent(
				tenantId,
				FeedbackEvents.AGGREGATE_TYPE,
				feedback.getId().value(),
				FeedbackEvents.FEEDBACK_CREATED,
				FeedbackEvents.FEEDBACK_CREATED_VERSION,
				OutboxPayloadFactory.feedbackCreatedPayload(feedback)
		);
		
		return feedback;
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
	
	@Transactional
	public Feedback updateStatus(UUID feedbackId, FeedbackStatus status) {
		TenantId tenantId = currentTenantProvider.getCurrentTenantId();
		Feedback feedback = feedbackRepository.updateStatus(tenantId, FeedbackId.of(feedbackId), status);
		publishUpdatedEvent(tenantId, feedback);
		return feedback;
	}
	
	@Transactional
	public Feedback updatePriority(UUID feedbackId, FeedbackPriority priority) {
		TenantId tenantId = currentTenantProvider.getCurrentTenantId();
		Feedback feedback = feedbackRepository.updatePriority(tenantId, FeedbackId.of(feedbackId), priority);
		publishUpdatedEvent(tenantId, feedback);
		return feedback;
	}
	
	@Transactional
	public Feedback assign(UUID feedbackId, String assignedTo) {
		TenantId tenantId = currentTenantProvider.getCurrentTenantId();
		Feedback feedback = feedbackRepository.assignTo(tenantId, FeedbackId.of(feedbackId), assignedTo);
		publishUpdatedEvent(tenantId, feedback);
		return feedback;
	}
	
	@Transactional
	public Feedback archive(UUID feedbackId) {
		TenantId tenantId = currentTenantProvider.getCurrentTenantId();
		Feedback feedback = feedbackRepository.archive(tenantId, FeedbackId.of(feedbackId));
		publishUpdatedEvent(tenantId, feedback);
		return feedback;
	}
	
	@Transactional
	public Feedback restore(UUID feedbackId) {
		TenantId tenantId = currentTenantProvider.getCurrentTenantId();
		Feedback feedback = feedbackRepository.restore(tenantId, FeedbackId.of(feedbackId));
		publishUpdatedEvent(tenantId, feedback);
		return feedback;
	}
	
	@Transactional
	public FeedbackNote addNote(UUID feedbackId, String content) {
		TenantId tenantId = currentTenantProvider.getCurrentTenantId();
		FeedbackId resolvedFeedbackId = FeedbackId.of(feedbackId);
		if (feedbackRepository.findByTenantIdAndId(tenantId, resolvedFeedbackId).isEmpty()) {
			throw new FeedbackNotFoundException(feedbackId);
		}
		return feedbackRepository.addNote(tenantId, resolvedFeedbackId, currentUserProvider.getCurrentUser().username(), content);
	}
	
	@Transactional(readOnly = true)
	public List<FeedbackNote> listNotes(UUID feedbackId) {
		TenantId tenantId = currentTenantProvider.getCurrentTenantId();
		FeedbackId resolvedFeedbackId = FeedbackId.of(feedbackId);
		if (feedbackRepository.findByTenantIdAndId(tenantId, resolvedFeedbackId).isEmpty()) {
			throw new FeedbackNotFoundException(feedbackId);
		}
		return feedbackRepository.listNotes(tenantId, resolvedFeedbackId);
	}
	
	private void publishUpdatedEvent(TenantId tenantId, Feedback feedback) {
		outboxEventRepository.savePendingEvent(
				tenantId,
				FeedbackEvents.AGGREGATE_TYPE,
				feedback.getId().value(),
				FeedbackEvents.FEEDBACK_UPDATED,
				FeedbackEvents.FEEDBACK_UPDATED_VERSION,
				OutboxPayloadFactory.feedbackUpdatedPayload(feedback)
		);
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
