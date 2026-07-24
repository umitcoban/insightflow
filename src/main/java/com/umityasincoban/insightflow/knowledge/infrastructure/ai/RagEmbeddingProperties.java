package com.umityasincoban.insightflow.knowledge.infrastructure.ai;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "insightflow.rag.embeddings")
public class RagEmbeddingProperties {
	
	private String provider = "spring-ai";
	private String model = "mxbai-embed-large";
	private int dimensions = 1024;
	
	public String getProvider() {
		return provider;
	}
	
	public void setProvider(String provider) {
		this.provider = provider;
	}
	
	public String getModel() {
		return model;
	}
	
	public void setModel(String model) {
		this.model = model;
	}
	
	public int getDimensions() {
		return Math.max(1, dimensions);
	}
	
	public void setDimensions(int dimensions) {
		this.dimensions = dimensions;
	}
}
