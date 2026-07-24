package com.umityasincoban.insightflow.knowledge.application;

import com.umityasincoban.insightflow.knowledge.domain.KnowledgeDocument;
import com.umityasincoban.insightflow.knowledge.domain.KnowledgeRepository;
import com.umityasincoban.insightflow.shared.api.PageResponse;
import com.umityasincoban.insightflow.shared.tenancy.CurrentTenantProvider;
import com.umityasincoban.insightflow.tenancy.domain.TenantId;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class KnowledgeApplicationService {
	
	private static final int CHUNK_SIZE = 1200;
	private static final int CHUNK_OVERLAP = 200;
	
	private final KnowledgeRepository knowledgeRepository;
	private final KnowledgeVectorStore vectorStore;
	private final EmbeddingClient embeddingClient;
	private final CurrentTenantProvider currentTenantProvider;
	
	public KnowledgeApplicationService(
			KnowledgeRepository knowledgeRepository,
			KnowledgeVectorStore vectorStore,
			EmbeddingClient embeddingClient,
			CurrentTenantProvider currentTenantProvider
	) {
		this.knowledgeRepository = knowledgeRepository;
		this.vectorStore = vectorStore;
		this.embeddingClient = embeddingClient;
		this.currentTenantProvider = currentTenantProvider;
	}
	
	@Transactional
	public KnowledgeDocument createDocument(String title, String source, String content, Map<String, Object> metadata) {
		TenantId tenantId = currentTenantProvider.getCurrentTenantId();
		KnowledgeDocument document = knowledgeRepository.saveDocument(tenantId, title, source, content, metadata);
		int index = 0;
		for (String chunkText : chunk(content)) {
			var chunk = knowledgeRepository.saveChunk(tenantId, document.id(), index++, chunkText);
			vectorStore.index(chunk, document.title(), document.source(), embeddingClient.embed(chunk.content()));
		}
		return document;
	}
	
	@Transactional(readOnly = true)
	public PageResponse<KnowledgeDocument> listDocuments(Integer page, Integer size) {
		int resolvedPage = page == null || page < 0 ? 0 : page;
		int resolvedSize = size == null || size < 1 ? 20 : Math.min(size, 100);
		return PageResponse.from(
				knowledgeRepository.findByTenantId(
						currentTenantProvider.getCurrentTenantId(),
						PageRequest.of(resolvedPage, resolvedSize, Sort.by(Sort.Direction.DESC, "createdAt"))
				),
				document -> document
		);
	}
	
	@Transactional
	public void deleteDocument(UUID documentId) {
		TenantId tenantId = currentTenantProvider.getCurrentTenantId();
		knowledgeRepository.deleteByTenantIdAndId(tenantId, documentId);
		vectorStore.deleteDocument(tenantId, documentId);
	}
	
	private static List<String> chunk(String content) {
		if (content == null || content.isBlank()) {
			throw new IllegalArgumentException("Knowledge document content cannot be blank");
		}
		List<String> chunks = new ArrayList<>();
		int start = 0;
		while (start < content.length()) {
			int end = Math.min(content.length(), start + CHUNK_SIZE);
			chunks.add(content.substring(start, end));
			if (end == content.length()) {
				break;
			}
			start = Math.max(0, end - CHUNK_OVERLAP);
		}
		return chunks;
	}
}

