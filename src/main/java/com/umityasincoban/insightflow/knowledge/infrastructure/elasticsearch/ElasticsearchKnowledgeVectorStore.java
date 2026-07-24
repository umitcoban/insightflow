package com.umityasincoban.insightflow.knowledge.infrastructure.elasticsearch;

import com.umityasincoban.insightflow.knowledge.application.KnowledgeVectorStore;
import com.umityasincoban.insightflow.knowledge.application.RetrievedKnowledgeChunk;
import com.umityasincoban.insightflow.knowledge.domain.KnowledgeChunk;
import com.umityasincoban.insightflow.knowledge.infrastructure.ai.RagEmbeddingProperties;
import com.umityasincoban.insightflow.search.infrastructure.elasticsearch.ElasticsearchProperties;
import com.umityasincoban.insightflow.search.infrastructure.elasticsearch.ElasticsearchRestClientFactory;
import com.umityasincoban.insightflow.tenancy.domain.TenantId;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class ElasticsearchKnowledgeVectorStore implements KnowledgeVectorStore {
	
	private final ElasticsearchProperties properties;
	private final RagEmbeddingProperties embeddingProperties;
	private final RestClient restClient;
	private final ObjectMapper objectMapper;
	
	public ElasticsearchKnowledgeVectorStore(
			ElasticsearchProperties properties,
			RagEmbeddingProperties embeddingProperties,
			ElasticsearchRestClientFactory restClientFactory,
			ObjectMapper objectMapper
	) {
		this.properties = properties;
		this.embeddingProperties = embeddingProperties;
		this.restClient = restClientFactory.create();
		this.objectMapper = objectMapper;
	}
	
	@PostConstruct
	void ensureIndex() {
		try {
			restClient.head().uri("/{index}", properties.getKnowledgeIndex()).retrieve().toBodilessEntity();
		} catch (RestClientResponseException exception) {
			if (exception.getStatusCode().value() == 404) {
				restClient.put().uri("/{index}", properties.getKnowledgeIndex()).body(mapping()).retrieve().toBodilessEntity();
			}
		} catch (RestClientException ignored) {
			// Elasticsearch may be offline in unit-test or local API-only runs.
		}
	}
	
	@Override
	public void index(KnowledgeChunk chunk, String documentTitle, String source, List<Double> embedding) {
		restClient.put()
				.uri("/{index}/_doc/{id}", properties.getKnowledgeIndex(), chunk.id().toString())
				.body(Map.of(
						"id", chunk.id().toString(),
						"tenantId", chunk.tenantId().value().toString(),
						"documentId", chunk.documentId().toString(),
						"chunkIndex", chunk.chunkIndex(),
						"documentTitle", documentTitle,
						"source", source,
						"content", chunk.content(),
						"embedding", embedding,
						"createdAt", chunk.createdAt().toString()
				))
				.retrieve()
				.toBodilessEntity();
	}
	
	@Override
	public void deleteDocument(TenantId tenantId, UUID documentId) {
		try {
			restClient.post()
					.uri("/{index}/_delete_by_query", properties.getKnowledgeIndex())
					.body(Map.of("query", Map.of("bool", Map.of("filter", List.of(
							Map.of("term", Map.of("tenantId", tenantId.value().toString())),
							Map.of("term", Map.of("documentId", documentId.toString()))
					)))))
					.retrieve()
					.toBodilessEntity();
		} catch (RestClientResponseException exception) {
			if (exception.getStatusCode().value() != 404) {
				throw exception;
			}
		} catch (RestClientException ignored) {
			// Deleting the source document should remain possible when the vector index is unavailable.
		}
	}
	
	@Override
	public List<RetrievedKnowledgeChunk> search(TenantId tenantId, List<Double> queryEmbedding, int limit) {
		Map<String, Object> response = restClient.post()
				.uri("/{index}/_search", properties.getKnowledgeIndex())
				.body(Map.of(
						"size", limit,
						"query", Map.of("script_score", Map.of(
								"query", Map.of("term", Map.of("tenantId", tenantId.value().toString())),
								"script", Map.of(
										"source", "cosineSimilarity(params.query_vector, 'embedding') + 1.0",
										"params", Map.of("query_vector", queryEmbedding)
								)
						))
				))
				.retrieve()
				.body(Map.class);
		JsonNode hits = objectMapper.valueToTree(response == null ? Map.of() : response).path("hits").path("hits");
		List<RetrievedKnowledgeChunk> results = new ArrayList<>();
		if (hits.isArray()) {
			for (JsonNode hit : hits) {
				JsonNode source = hit.path("_source");
				results.add(new RetrievedKnowledgeChunk(
						UUID.fromString(source.path("documentId").asText()),
						UUID.fromString(source.path("id").asText()),
						source.path("documentTitle").asText(),
						source.path("source").asText(),
						source.path("content").asText(),
						hit.path("_score").asDouble()
				));
			}
		}
		return results;
	}
	
	private Map<String, Object> mapping() {
		return Map.of("mappings", Map.of("properties", Map.of(
				"id", Map.of("type", "keyword"),
				"tenantId", Map.of("type", "keyword"),
				"documentId", Map.of("type", "keyword"),
				"chunkIndex", Map.of("type", "integer"),
				"documentTitle", Map.of("type", "text"),
				"source", Map.of("type", "keyword"),
				"content", Map.of("type", "text"),
				"embedding", Map.of("type", "dense_vector", "dims", embeddingProperties.getDimensions()),
				"createdAt", Map.of("type", "date")
		)));
	}
}
