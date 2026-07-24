package com.umityasincoban.insightflow.shared.observability;

import com.umityasincoban.insightflow.ai.infrastructure.AiProperties;
import com.umityasincoban.insightflow.knowledge.infrastructure.ai.RagEmbeddingProperties;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component("ai")
public class AiHealthIndicator implements HealthIndicator {
	
	private final AiProperties aiProperties;
	private final RagEmbeddingProperties embeddingProperties;
	private final Environment environment;
	
	public AiHealthIndicator(
			AiProperties aiProperties,
			RagEmbeddingProperties embeddingProperties,
			Environment environment
	) {
		this.aiProperties = aiProperties;
		this.embeddingProperties = embeddingProperties;
		this.environment = environment;
	}
	
	@Override
	public Health health() {
		String chatBackend = environment.getProperty("spring.ai.model.chat", "none");
		String embeddingBackend = environment.getProperty("spring.ai.model.embedding", "none");
		Health.Builder builder = Health.up()
				.withDetail("provider", aiProperties.getProvider())
				.withDetail("chatBackend", chatBackend)
				.withDetail("embeddingBackend", embeddingBackend)
				.withDetail("embeddingProvider", embeddingProperties.getProvider())
				.withDetail("embeddingModel", embeddingProperties.getModel())
				.withDetail("embeddingDimensions", embeddingProperties.getDimensions());
		if ("openai".equalsIgnoreCase(chatBackend) || "openai".equalsIgnoreCase(embeddingBackend)) {
			String apiKey = environment.getProperty("spring.ai.openai.api-key", "");
			if (apiKey.isBlank()) {
				return builder.down().withDetail("reason", "spring.ai.openai.api-key is not configured").build();
			}
		}
		return builder.build();
	}
}
