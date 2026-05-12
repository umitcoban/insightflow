package com.umityasincoban.insightflow.shared.observability;

public final class CorrelationId {
	
	public static final String HEADER_NAME = "X-Correlation-Id";
	public static final String MDC_KEY = "correlationId";
	
	private CorrelationId() {
	}
}