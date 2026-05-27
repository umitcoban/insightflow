package com.umityasincoban.insightflow.feedback.domain;

public final class FeedbackEvents {
	
	public static final String AGGREGATE_TYPE = "FEEDBACK";
	
	public static final String FEEDBACK_CREATED = "feedback.created";
	public static final int FEEDBACK_CREATED_VERSION = 1;
	
	public static final String AI_ANALYSIS_COMPLETED = "feedback.ai-analysis-completed";
	public static final int AI_ANALYSIS_COMPLETED_VERSION = 1;
	
	private FeedbackEvents() {
	}
}