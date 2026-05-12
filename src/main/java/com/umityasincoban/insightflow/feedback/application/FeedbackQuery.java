package com.umityasincoban.insightflow.feedback.application;

import com.umityasincoban.insightflow.feedback.domain.FeedbackPriority;
import com.umityasincoban.insightflow.feedback.domain.FeedbackStatus;

public record FeedbackQuery(
		FeedbackStatus status,
		FeedbackPriority priority,
		int page,
		int size
) {
	
	private static final int DEFAULT_PAGE = 0;
	private static final int DEFAULT_SIZE = 20;
	private static final int MAX_SIZE = 100;
	
	public FeedbackQuery {
		if (page < 0) {
			page = DEFAULT_PAGE;
		}
		
		if (size <= 0) {
			size = DEFAULT_SIZE;
		}
		
		if (size > MAX_SIZE) {
			size = MAX_SIZE;
		}
	}
	
	public static FeedbackQuery of(
			FeedbackStatus status,
			FeedbackPriority priority,
			Integer page,
			Integer size
	) {
		return new FeedbackQuery(
				status,
				priority,
				page == null ? DEFAULT_PAGE : page,
				size == null ? DEFAULT_SIZE : size
		);
	}
}