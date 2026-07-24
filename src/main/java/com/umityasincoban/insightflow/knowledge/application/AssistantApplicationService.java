package com.umityasincoban.insightflow.knowledge.application;

import com.umityasincoban.insightflow.knowledge.domain.KnowledgeChunk;
import com.umityasincoban.insightflow.knowledge.domain.KnowledgeDocument;
import com.umityasincoban.insightflow.knowledge.domain.KnowledgeRepository;
import com.umityasincoban.insightflow.shared.tenancy.CurrentTenantProvider;
import com.umityasincoban.insightflow.tenancy.domain.TenantId;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class AssistantApplicationService {
	
	private final EmbeddingClient embeddingClient;
	private final KnowledgeVectorStore vectorStore;
	private final AnswerGenerator answerGenerator;
	private final CurrentTenantProvider currentTenantProvider;
	private final KnowledgeRepository knowledgeRepository;
	
	public AssistantApplicationService(
			EmbeddingClient embeddingClient,
			KnowledgeVectorStore vectorStore,
			AnswerGenerator answerGenerator,
			CurrentTenantProvider currentTenantProvider,
			KnowledgeRepository knowledgeRepository
	) {
		this.embeddingClient = embeddingClient;
		this.vectorStore = vectorStore;
		this.answerGenerator = answerGenerator;
		this.currentTenantProvider = currentTenantProvider;
		this.knowledgeRepository = knowledgeRepository;
	}
	
	public AssistantAnswer answer(String question) {
		var tenantId = currentTenantProvider.getCurrentTenantId();
		List<RetrievedKnowledgeChunk> chunks = semanticSearch(tenantId, question);
		if (chunks.isEmpty()) {
			chunks = lexicalFallback(tenantId, question);
		}
		if (chunks.isEmpty()) {
			return new AssistantAnswer("Knowledge base does not contain enough information to answer this question.", List.of());
		}
		try {
			return new AssistantAnswer(answerGenerator.answer(question, chunks), chunks);
		} catch (RuntimeException exception) {
			throw new KnowledgeAssistantUnavailableException("Knowledge assistant found relevant sources, but the AI model could not generate an answer. Verify the Ollama chat model configuration.", exception);
		}
	}
	
	private List<RetrievedKnowledgeChunk> semanticSearch(TenantId tenantId, String question) {
		try {
			return vectorStore.search(
					tenantId,
					embeddingClient.embed(question),
					5
			);
		} catch (RuntimeException exception) {
			return List.of();
		}
	}
	
	private List<RetrievedKnowledgeChunk> lexicalFallback(TenantId tenantId, String question) {
		Set<String> queryTerms = tokenize(question);
		if (queryTerms.isEmpty()) {
			return List.of();
		}
		List<KnowledgeChunk> chunks = knowledgeRepository.findChunksByTenantId(tenantId);
		var documents = chunks.stream()
				.map(KnowledgeChunk::documentId)
				.distinct()
				.map(documentId -> knowledgeRepository.findByTenantIdAndId(tenantId, documentId))
				.flatMap(java.util.Optional::stream)
				.collect(Collectors.toMap(KnowledgeDocument::id, Function.identity()));
		
		return chunks.stream()
				.map(chunk -> toRetrievedChunk(chunk, documents.get(chunk.documentId()), queryTerms))
				.filter(scored -> scored.score() > 0)
				.sorted(Comparator.comparingDouble(ScoredKnowledgeChunk::score).reversed())
				.limit(5)
				.map(ScoredKnowledgeChunk::chunk)
				.toList();
	}
	
	private static ScoredKnowledgeChunk toRetrievedChunk(
			KnowledgeChunk chunk,
			KnowledgeDocument document,
			Set<String> queryTerms
	) {
		String title = document == null ? "Knowledge document" : document.title();
		String source = document == null ? "knowledge-base" : document.source();
		double score = score(chunk.content(), queryTerms) + score(title, queryTerms) * 2;
		return new ScoredKnowledgeChunk(
				new RetrievedKnowledgeChunk(chunk.documentId(), chunk.id(), title, source, chunk.content(), score),
				score
		);
	}
	
	private static double score(String text, Set<String> queryTerms) {
		Set<String> terms = tokenize(text);
		return queryTerms.stream().filter(terms::contains).count();
	}
	
	private static Set<String> tokenize(String text) {
		if (text == null || text.isBlank()) {
			return Set.of();
		}
		return List.of(text.toLowerCase(Locale.ROOT).split("[^a-z0-9]+"))
				.stream()
				.filter(term -> term.length() > 2)
				.collect(Collectors.toSet());
	}
	
	private record ScoredKnowledgeChunk(RetrievedKnowledgeChunk chunk, double score) {
	}
}
