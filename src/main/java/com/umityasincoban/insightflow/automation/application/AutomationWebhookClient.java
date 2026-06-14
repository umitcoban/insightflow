package com.umityasincoban.insightflow.automation.application;

public interface AutomationWebhookClient {
	
	AutomationWebhookResponse send(AutomationWebhookRequest request);
}
