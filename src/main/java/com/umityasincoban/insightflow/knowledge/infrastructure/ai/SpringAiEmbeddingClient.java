package com.umityasincoban.insightflow.knowledge.infrastructure.ai;

import com.umityasincoban.insightflow.knowledge.application.EmbeddingClient;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@ConditionalOnProperty(prefix = "insightflow.rag.embeddings", name = "provider", havingValue = "spring-ai", matchIfMissing = true)
public class SpringAiEmbeddingClient implements EmbeddingClient {
	
	private final EmbeddingModel embeddingModel;
	
	public SpringAiEmbeddingClient(EmbeddingModel embeddingModel) {
		this.embeddingModel = embeddingModel;
	}
	
	@Override
	public List<Double> embed(String text) {
		float[] embedding = embeddingModel.embed(text);
		List<Double> vector = new ArrayList<>(embedding.length);
		for (float value : embedding) {
			vector.add((double) value);
		}
		return vector;
	}
}
