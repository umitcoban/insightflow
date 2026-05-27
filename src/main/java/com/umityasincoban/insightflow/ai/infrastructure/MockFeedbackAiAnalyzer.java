package com.umityasincoban.insightflow.ai.infrastructure;

import com.umityasincoban.insightflow.ai.domain.FeedbackAiAnalysisResult;
import com.umityasincoban.insightflow.ai.domain.FeedbackAiAnalyzer;
import com.umityasincoban.insightflow.feedback.domain.FeedbackRiskLevel;
import com.umityasincoban.insightflow.feedback.domain.FeedbackSentiment;
import org.springframework.stereotype.Component;

@Component
public class MockFeedbackAiAnalyzer implements FeedbackAiAnalyzer {
	
	@Override
	public FeedbackAiAnalysisResult analyze(String title, String content) {
		String text = (title + " " + content).toLowerCase();
		
		FeedbackSentiment sentiment = detectSentiment(text);
		String category = detectCategory(text);
		FeedbackRiskLevel riskLevel = detectRiskLevel(text, sentiment);
		String summary = buildSummary(title, category, sentiment);
		String suggestedAction = buildSuggestedAction(category, riskLevel);
		
		return new FeedbackAiAnalysisResult(
				sentiment,
				category,
				riskLevel,
				summary,
				suggestedAction
		);
	}
	
	private static FeedbackSentiment detectSentiment(String text) {
		if (containsAny(text, "not work", "failed", "crash", "cannot", "does not", "locked", "error", "problem")) {
			return FeedbackSentiment.NEGATIVE;
		}
		
		if (containsAny(text, "great", "love", "good", "thanks", "excellent")) {
			return FeedbackSentiment.POSITIVE;
		}
		
		return FeedbackSentiment.NEUTRAL;
	}
	
	private static String detectCategory(String text) {
		if (containsAny(text, "payment", "purchase", "premium", "receipt", "checkout", "restore")) {
			return "PAYMENT";
		}
		
		if (containsAny(text, "login", "password", "sign in", "authentication")) {
			return "AUTHENTICATION";
		}
		
		if (containsAny(text, "crash", "freeze", "slow", "performance")) {
			return "STABILITY";
		}
		
		return "GENERAL";
	}
	
	private static FeedbackRiskLevel detectRiskLevel(String text, FeedbackSentiment sentiment) {
		if (containsAny(text, "cancel", "refund", "angry", "churn") || FeedbackSentiment.NEGATIVE.equals(sentiment)) {
			return FeedbackRiskLevel.HIGH;
		}
		
		if (FeedbackSentiment.POSITIVE.equals(sentiment)) {
			return FeedbackRiskLevel.LOW;
		}
		
		return FeedbackRiskLevel.MEDIUM;
	}
	
	private static String buildSummary(String title, String category, FeedbackSentiment sentiment) {
		return "AI mock summary: feedback categorized as " + category
				+ " with " + sentiment + " sentiment. Title: " + title;
	}
	
	private static String buildSuggestedAction(String category, FeedbackRiskLevel riskLevel) {
		if ("PAYMENT".equals(category)) {
			return "Check payment provider logs, receipt validation flow, and premium entitlement synchronization.";
		}
		
		if ("AUTHENTICATION".equals(category)) {
			return "Review authentication logs, login error rates, and password reset delivery status.";
		}
		
		if ("STABILITY".equals(category)) {
			return "Inspect crash reports, app version metrics, and recent release changes.";
		}
		
		if (FeedbackRiskLevel.HIGH.equals(riskLevel)) {
			return "Prioritize this feedback and assign it to a support owner.";
		}
		
		return "Review the feedback and classify it for follow-up.";
	}
	
	private static boolean containsAny(String text, String... keywords) {
		for (String keyword : keywords) {
			if (text.contains(keyword)) {
				return true;
			}
		}
		
		return false;
	}
}