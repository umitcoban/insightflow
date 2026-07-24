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
		var tenantId = currentTenantProvider.getCurrentTenantId();
		try {
			List<RetrievedKnowledgeChunk> chunks = vectorStore.search(
					tenantId,
					embeddingClient.embed(question),
					5
			);
			if (chunks.isEmpty()) {
				return new AssistantAnswer("Knowledge base does not contain enough information to answer this question.", chunks);
			}
			return new AssistantAnswer(answerGenerator.answer(question, chunks), chunks);
		} catch (RuntimeException exception) {
			throw new KnowledgeAssistantUnavailableException("Knowledge assistant is unavailable. Verify Ollama models and Elasticsearch vector index configuration.", exception);
		}
	}
}
