package com.umityasincoban.insightflow.ai.infrastructure;

import com.umityasincoban.insightflow.ai.domain.FeedbackAiAnalysisResult;
import com.umityasincoban.insightflow.ai.domain.FeedbackAiAnalyzer;
import com.umityasincoban.insightflow.feedback.domain.FeedbackRiskLevel;
import com.umityasincoban.insightflow.feedback.domain.FeedbackSentiment;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "insightflow.ai", name = "provider", havingValue = "spring-ai")
public class SpringAiFeedbackAiAnalyzer implements FeedbackAiAnalyzer {
	
	private final ChatClient chatClient;
	
	public SpringAiFeedbackAiAnalyzer(ChatClient.Builder chatClientBuilder) {
		this.chatClient = chatClientBuilder.build();
	}
	
	@Override
	public FeedbackAiAnalysisResult analyze(String title, String content) {
		FeedbackAiAnalysisResponse response = chatClient.prompt()
				.system("""
						Analyze customer feedback for a SaaS support team.
						Use POSITIVE, NEUTRAL, or NEGATIVE for sentiment.
						Use LOW, MEDIUM, HIGH, or CHURN_RISK for riskLevel.
						Keep category short, uppercase, and business-oriented.
						""")
				.user("Title: " + title + "\n\nContent: " + content)
				.call()
				.entity(FeedbackAiAnalysisResponse.class, spec -> spec.validateSchema());
		
		if (response == null) {
			throw new FeedbackAiAnalysisException("Spring AI response did not contain feedback analysis");
		}
		return new FeedbackAiAnalysisResult(
				FeedbackSentiment.valueOf(response.sentiment()),
				response.category(),
				FeedbackRiskLevel.valueOf(response.riskLevel()),
				response.summary(),
				response.suggestedAction()
		);
	}
	
	public record FeedbackAiAnalysisResponse(
			String sentiment,
			String category,
			String riskLevel,
			String summary,
			String suggestedAction
	) {
	}
}
