package com.umityasincoban.insightflow.automation.infrastructure.scheduling;

import com.umityasincoban.insightflow.automation.application.AutomationWebhookRetryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class AutomationWebhookRetryScheduler {
	
	private static final Logger log = LoggerFactory.getLogger(AutomationWebhookRetryScheduler.class);
	
	private final AutomationWebhookRetryService retryService;
	
	public AutomationWebhookRetryScheduler(AutomationWebhookRetryService retryService) {
		this.retryService = retryService;
	}
	
	@Scheduled(fixedDelayString = "${insightflow.automation.webhook.retry.scheduler-fixed-delay-ms:2000}")
	public void retryDueWebhookActions() {
		try {
			int claimedCount = retryService.retryDueWebhookActions();
			if (claimedCount > 0) {
				log.debug("Claimed webhook action retries count={}", claimedCount);
			}
		} catch (RuntimeException exception) {
			log.error("Webhook retry scheduler failed", exception);
		}
	}
}
