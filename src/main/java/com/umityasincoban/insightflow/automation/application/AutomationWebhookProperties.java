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
	private final Retry retry = new Retry();
	
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
	
	public Retry getRetry() {
		return retry;
	}
	
	public static class Retry {
		
		private boolean enabled = true;
		private int maxAttempts = 3;
		private long initialDelayMs = 5000;
		private double multiplier = 2.0;
		private long maxDelayMs = 300000;
		private long schedulerFixedDelayMs = 2000;
		private int batchSize = 20;
		private long inProgressTimeoutMs = 60000;
		
		public boolean isEnabled() {
			return enabled;
		}
		
		public void setEnabled(boolean enabled) {
			this.enabled = enabled;
		}
		
		public int getMaxAttempts() {
			return Math.max(1, maxAttempts);
		}
		
		public void setMaxAttempts(int maxAttempts) {
			this.maxAttempts = maxAttempts;
		}
		
		public long getInitialDelayMs() {
			return Math.max(1, initialDelayMs);
		}
		
		public void setInitialDelayMs(long initialDelayMs) {
			this.initialDelayMs = initialDelayMs;
		}
		
		public double getMultiplier() {
			return Math.max(1.0, multiplier);
		}
		
		public void setMultiplier(double multiplier) {
			this.multiplier = multiplier;
		}
		
		public long getMaxDelayMs() {
			return Math.max(getInitialDelayMs(), maxDelayMs);
		}
		
		public void setMaxDelayMs(long maxDelayMs) {
			this.maxDelayMs = maxDelayMs;
		}
		
		public long getSchedulerFixedDelayMs() {
			return Math.max(1, schedulerFixedDelayMs);
		}
		
		public void setSchedulerFixedDelayMs(long schedulerFixedDelayMs) {
			this.schedulerFixedDelayMs = schedulerFixedDelayMs;
		}
		
		public int getBatchSize() {
			return Math.max(1, batchSize);
		}
		
		public void setBatchSize(int batchSize) {
			this.batchSize = batchSize;
		}
		
		public long getInProgressTimeoutMs() {
			return Math.max(1, inProgressTimeoutMs);
		}
		
		public void setInProgressTimeoutMs(long inProgressTimeoutMs) {
			this.inProgressTimeoutMs = inProgressTimeoutMs;
		}
	}
}
