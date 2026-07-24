package com.umityasincoban.insightflow.knowledge.application;

import com.umityasincoban.insightflow.shared.tenancy.CurrentTenantProvider;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AssistantApplicationService {
	
	private final EmbeddingClient embeddingClient;
	private final KnowledgeVectorStore vectorStore;
	private final AnswerGenerator answerGenerator;
	private final CurrentTenantProvider currentTenantProvider;
	
	public AssistantApplicationService(
			EmbeddingClient embeddingClient,
			KnowledgeVectorStore vectorStore,
			AnswerGenerator answerGenerator,
			CurrentTenantProvider currentTenantProvider
	) {
		this.embeddingClient = embeddingClient;
		this.vectorStore = vectorStore;
		this.answerGenerator = answerGenerator;
		this.currentTenantProvider = currentTenantProvider;
	}
	
	public AssistantAnswer answer(String question) {
		List<RetrievedKnowledgeChunk> chunks = vectorStore.search(
				currentTenantProvider.getCurrentTenantId(),
				embeddingClient.embed(question),
				5
		);
		return new AssistantAnswer(answerGenerator.answer(question, chunks), chunks);
	}
}

