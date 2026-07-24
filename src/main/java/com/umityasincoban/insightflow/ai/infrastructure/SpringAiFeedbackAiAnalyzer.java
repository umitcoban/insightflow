package com.umityasincoban.insightflow.ai.infrastructure;

import com.umityasincoban.insightflow.ai.domain.FeedbackAiAnalysisResult;
import com.umityasincoban.insightflow.ai.domain.FeedbackAiAnalyzer;
import com.umityasincoban.insightflow.feedback.domain.FeedbackRiskLevel;
import com.umityasincoban.insightflow.feedback.domain.FeedbackSentiment;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Component
@ConditionalOnProperty(prefix = "insightflow.ai", name = "provider", havingValue = "spring-ai")
public class SpringAiFeedbackAiAnalyzer implements FeedbackAiAnalyzer {
	
	private final ChatClient chatClient;
	private final ObjectMapper objectMapper;
	
	public SpringAiFeedbackAiAnalyzer(ChatClient.Builder chatClientBuilder, ObjectMapper objectMapper) {
		this.chatClient = chatClientBuilder.build();
		this.objectMapper = objectMapper;
	}
	
	@Override
	public FeedbackAiAnalysisResult analyze(String title, String content) {
		String response = chatClient.prompt()
				.system("""
						Analyze customer feedback for a SaaS support team.
						Return exactly one JSON object and no markdown, no code fence, no explanation.
						The JSON object must contain these string fields:
						- sentiment: one of POSITIVE, NEUTRAL, NEGATIVE
						- category: short, uppercase, business-oriented category
						- riskLevel: one of LOW, MEDIUM, HIGH, CHURN_RISK
						- summary: concise operational summary
						- suggestedAction: concrete next support action
						""")
				.user("""
						Analyze this feedback.
						
						Title:
						%s
						
						Content:
						%s
						""".formatted(title, content))
				.call()
				.content();
		
		try {
			FeedbackAiAnalysisResponse parsedResponse = objectMapper.readValue(
					extractJsonObject(response),
					FeedbackAiAnalysisResponse.class
			);
			return toResult(parsedResponse);
		} catch (JacksonException | IllegalArgumentException exception) {
			throw new FeedbackAiAnalysisException("Spring AI response did not contain valid feedback analysis JSON", exception);
		}
	}
	
	private static FeedbackAiAnalysisResult toResult(FeedbackAiAnalysisResponse response) {
		if (response == null) {
			throw new FeedbackAiAnalysisException("Spring AI response did not contain feedback analysis");
		}
		return new FeedbackAiAnalysisResult(
				FeedbackSentiment.valueOf(required(response.sentiment(), "sentiment").toUpperCase()),
				required(response.category(), "category").toUpperCase(),
				FeedbackRiskLevel.valueOf(required(response.riskLevel(), "riskLevel").toUpperCase()),
				required(response.summary(), "summary"),
				required(response.suggestedAction(), "suggestedAction")
		);
	}
	
	private static String extractJsonObject(String response) {
		if (response == null || response.isBlank()) {
			throw new FeedbackAiAnalysisException("Spring AI response was empty");
		}
		
		int start = response.indexOf('{');
		if (start < 0) {
			throw new FeedbackAiAnalysisException("Spring AI response did not contain a JSON object");
		}
		
		boolean inString = false;
		boolean escaped = false;
		int depth = 0;
		for (int index = start; index < response.length(); index++) {
			char current = response.charAt(index);
			if (escaped) {
				escaped = false;
				continue;
			}
			if (current == '\\') {
				escaped = true;
				continue;
			}
			if (current == '"') {
				inString = !inString;
				continue;
			}
			if (inString) {
				continue;
			}
			if (current == '{') {
				depth++;
			}
			if (current == '}') {
				depth--;
				if (depth == 0) {
					return response.substring(start, index + 1);
				}
			}
		}
		
		throw new FeedbackAiAnalysisException("Spring AI response JSON object was incomplete");
	}
	
	private static String required(String value, String fieldName) {
		if (value == null || value.isBlank()) {
			throw new FeedbackAiAnalysisException("Spring AI response is missing required field: " + fieldName);
		}
		return value.strip();
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
