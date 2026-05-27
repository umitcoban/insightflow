package com.umityasincoban.insightflow.ai.domain;

public interface FeedbackAiAnalyzer {
	
	FeedbackAiAnalysisResult analyze(String title, String content);
}