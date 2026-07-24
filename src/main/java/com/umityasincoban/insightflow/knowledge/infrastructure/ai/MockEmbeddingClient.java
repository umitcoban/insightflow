package com.umityasincoban.insightflow.knowledge.infrastructure.ai;

import com.umityasincoban.insightflow.knowledge.application.EmbeddingClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@ConditionalOnProperty(prefix = "insightflow.rag.embeddings", name = "provider", havingValue = "mock")
public class MockEmbeddingClient implements EmbeddingClient {
	
	private final RagEmbeddingProperties properties;
	
	public MockEmbeddingClient(RagEmbeddingProperties properties) {
		this.properties = properties;
	}
	
	@Override
	public List<Double> embed(String text) {
		List<Double> vector = new ArrayList<>();
		int seed = text == null ? 0 : text.hashCode();
		for (int i = 0; i < properties.getDimensions(); i++) {
			vector.add(((seed + i * 31) % 1000) / 1000.0);
		}
		return vector;
	}
}
