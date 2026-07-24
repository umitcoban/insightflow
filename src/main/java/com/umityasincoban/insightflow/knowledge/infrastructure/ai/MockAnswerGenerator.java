package com.umityasincoban.insightflow.knowledge.infrastructure.ai;

import com.umityasincoban.insightflow.knowledge.application.AnswerGenerator;
import com.umityasincoban.insightflow.knowledge.application.RetrievedKnowledgeChunk;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConditionalOnProperty(prefix = "insightflow.ai", name = "provider", havingValue = "mock", matchIfMissing = true)
public class MockAnswerGenerator implements AnswerGenerator {
	
	@Override
	public String answer(String question, List<RetrievedKnowledgeChunk> chunks) {
		if (chunks.isEmpty()) {
			return "Knowledge base does not contain enough information to answer this question.";
		}
		return "Mock answer based on " + chunks.size() + " retrieved knowledge chunk(s): " + chunks.getFirst().content();
	}
}
