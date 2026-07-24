package com.umityasincoban.insightflow.knowledge.application;

import java.util.List;

public interface AnswerGenerator {
	
	String answer(String question, List<RetrievedKnowledgeChunk> chunks);
}

