package com.umityasincoban.insightflow.knowledge.application;

import java.util.List;

public interface EmbeddingClient {
	
	List<Double> embed(String text);
}

