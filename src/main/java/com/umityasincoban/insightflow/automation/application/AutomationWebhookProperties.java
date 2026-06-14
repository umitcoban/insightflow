package com.umityasincoban.insightflow.automation.application;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "insightflow.automation.webhook")
public class AutomationWebhookProperties {
	
	private int defaultTimeoutMs = 5000;
	private int minTimeoutMs = 100;
	private int maxTimeoutMs = 30000;
	private int maxResponseBodyLength = 10000;
	
	public int getDefaultTimeoutMs() {
		return defaultTimeoutMs;
	}
	
	public void setDefaultTimeoutMs(int defaultTimeoutMs) {
		this.defaultTimeoutMs = defaultTimeoutMs;
	}
	
	public int getMinTimeoutMs() {
		return minTimeoutMs;
	}
	
	public void setMinTimeoutMs(int minTimeoutMs) {
		this.minTimeoutMs = minTimeoutMs;
	}
	
	public int getMaxTimeoutMs() {
		return maxTimeoutMs;
	}
	
	public void setMaxTimeoutMs(int maxTimeoutMs) {
		this.maxTimeoutMs = maxTimeoutMs;
	}
	
	public int getMaxResponseBodyLength() {
		return maxResponseBodyLength;
	}
	
	public void setMaxResponseBodyLength(int maxResponseBodyLength) {
		this.maxResponseBodyLength = maxResponseBodyLength;
	}
}
