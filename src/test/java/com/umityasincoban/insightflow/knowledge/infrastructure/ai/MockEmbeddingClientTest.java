package com.umityasincoban.insightflow.knowledge.infrastructure.ai;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MockEmbeddingClientTest {
	
	@Test
	void returnsStableDefaultDimensionVector() {
		RagEmbeddingProperties properties = new RagEmbeddingProperties();
		properties.setDimensions(1536);
		MockEmbeddingClient client = new MockEmbeddingClient(properties);
		
		assertThat(client.embed("hello")).hasSize(1536);
		assertThat(client.embed("hello")).isEqualTo(client.embed("hello"));
	}
}
