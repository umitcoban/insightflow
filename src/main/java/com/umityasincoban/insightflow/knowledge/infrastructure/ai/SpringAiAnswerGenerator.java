package com.umityasincoban.insightflow.knowledge.infrastructure.ai;

import com.umityasincoban.insightflow.knowledge.application.AnswerGenerator;
import com.umityasincoban.insightflow.knowledge.application.RetrievedKnowledgeChunk;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConditionalOnProperty(prefix = "insightflow.ai", name = "provider", havingValue = "spring-ai")
public class SpringAiAnswerGenerator implements AnswerGenerator {
	
	private final ChatClient chatClient;
	
	public SpringAiAnswerGenerator(ChatClient.Builder chatClientBuilder) {
		this.chatClient = chatClientBuilder.build();
	}
	
	@Override
	public String answer(String question, List<RetrievedKnowledgeChunk> chunks) {
		String answer = chatClient.prompt()
				.system("Answer only from the provided tenant knowledge chunks. If the answer is not present, say that the knowledge base does not contain enough information.")
				.user("Question: " + question + "\n\nSources:\n" + formatChunks(chunks))
				.call()
				.content();
		return answer == null || answer.isBlank()
				? "The model response did not contain an answer."
				: answer;
	}
	
	private static String formatChunks(List<RetrievedKnowledgeChunk> chunks) {
		StringBuilder builder = new StringBuilder();
		for (RetrievedKnowledgeChunk chunk : chunks) {
			builder.append("- ").append(chunk.documentTitle()).append(" [").append(chunk.chunkId()).append("]: ")
					.append(chunk.content()).append('\n');
		}
		return builder.toString();
	}
}
