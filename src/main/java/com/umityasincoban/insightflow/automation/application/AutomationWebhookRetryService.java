package com.umityasincoban.insightflow.automation.application;

import com.umityasincoban.insightflow.automation.domain.AutomationActionExecution;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;

@Service
public class AutomationWebhookRetryService {
	
	private final AutomationActionExecutionPersistenceService persistenceService;
	private final AutomationWebhookActionOrchestrator webhookActionOrchestrator;
	private final AutomationWebhookProperties properties;
	
	public AutomationWebhookRetryService(
			AutomationActionExecutionPersistenceService persistenceService,
			AutomationWebhookActionOrchestrator webhookActionOrchestrator,
			AutomationWebhookProperties properties
	) {
		this.persistenceService = persistenceService;
		this.webhookActionOrchestrator = webhookActionOrchestrator;
		this.properties = properties;
	}
	
	public int retryDueWebhookActions() {
		if (!properties.getRetry().isEnabled()) {
			return 0;
		}
		
		OffsetDateTime now = OffsetDateTime.now();
		OffsetDateTime staleBefore = now.minus(Duration.ofMillis(properties.getRetry().getInProgressTimeoutMs()));
		List<AutomationActionExecution> claimed = persistenceService.claimDueWebhookRetries(
				properties.getRetry().getBatchSize(),
				now,
				staleBefore
		);
		
		for (AutomationActionExecution actionExecution : claimed) {
			webhookActionOrchestrator.executeRetry(actionExecution);
		}
		
		return claimed.size();
	}
}
