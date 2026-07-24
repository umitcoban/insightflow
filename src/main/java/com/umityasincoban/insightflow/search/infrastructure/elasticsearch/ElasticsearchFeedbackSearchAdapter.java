package com.umityasincoban.insightflow.search.infrastructure.elasticsearch;

import com.umityasincoban.insightflow.search.application.FeedbackSearchDocument;
import com.umityasincoban.insightflow.search.application.FeedbackSearchPort;
import com.umityasincoban.insightflow.search.application.FeedbackSearchQuery;
import com.umityasincoban.insightflow.shared.api.PageResponse;
import com.umityasincoban.insightflow.tenancy.domain.TenantId;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class ElasticsearchFeedbackSearchAdapter implements FeedbackSearchPort {
	
	private final ElasticsearchProperties properties;
	private final RestClient restClient;
	private final ObjectMapper objectMapper;
	
	public ElasticsearchFeedbackSearchAdapter(
			ElasticsearchProperties properties,
			ElasticsearchRestClientFactory restClientFactory,
			ObjectMapper objectMapper
	) {
		this.properties = properties;
		this.restClient = restClientFactory.create();
		this.objectMapper = objectMapper;
	}
	
	@PostConstruct
	void ensureIndex() {
		try {
			restClient.head().uri("/{index}", properties.getFeedbackIndex()).retrieve().toBodilessEntity();
		} catch (RestClientResponseException exception) {
			if (exception.getStatusCode().value() == 404) {
				restClient.put().uri("/{index}", properties.getFeedbackIndex()).body(mapping()).retrieve().toBodilessEntity();
			}
		} catch (RestClientException ignored) {
			// Elasticsearch may be offline in unit-test or local API-only runs.
		}
	}
	
	@Override
	public void index(FeedbackSearchDocument document) {
		restClient.put()
				.uri("/{index}/_doc/{id}", properties.getFeedbackIndex(), document.id().toString())
				.body(document)
				.retrieve()
				.toBodilessEntity();
	}
	
	@Override
	public PageResponse<FeedbackSearchDocument> search(TenantId tenantId, FeedbackSearchQuery query) {
		Map<String, Object> request = searchRequest(tenantId, query);
		Map<String, Object> response = restClient.post()
				.uri("/{index}/_search", properties.getFeedbackIndex())
				.body(request)
				.retrieve()
				.body(Map.class);
		
		JsonNode root = objectMapper.valueToTree(response == null ? Map.of() : response);
		long total = root.path("hits").path("total").path("value").asLong(0);
		List<FeedbackSearchDocument> content = new ArrayList<>();
		JsonNode hits = root.path("hits").path("hits");
		if (hits.isArray()) {
			for (JsonNode hit : hits) {
				content.add(objectMapper.convertValue(hit.path("_source"), FeedbackSearchDocument.class));
			}
		}
		int totalPages = query.size() == 0 ? 0 : (int) Math.ceil((double) total / query.size());
		return new PageResponse<>(content, query.page(), query.size(), total, totalPages, query.page() == 0, query.page() + 1 >= totalPages);
	}
	
	private static Map<String, Object> searchRequest(TenantId tenantId, FeedbackSearchQuery query) {
		List<Map<String, Object>> filter = new ArrayList<>();
		filter.add(term("tenantId", tenantId.value().toString()));
		addTerm(filter, "status", query.status());
		addTerm(filter, "priority", query.priority());
		addTerm(filter, "sentiment", query.sentiment());
		addTerm(filter, "riskLevel", query.riskLevel());
		addTerm(filter, "source", query.source());
		if (query.customerId() != null) {
			filter.add(term("customerId", query.customerId().toString()));
		}
		if (query.category() != null && !query.category().isBlank()) {
			filter.add(term("category", query.category()));
		}
		if (query.from() != null || query.to() != null) {
			Map<String, Object> range = new LinkedHashMap<>();
			if (query.from() != null) {
				range.put("gte", query.from().toString());
			}
			if (query.to() != null) {
				range.put("lte", query.to().toString());
			}
			filter.add(Map.of("range", Map.of("createdAt", range)));
		}
		
		List<Map<String, Object>> must = new ArrayList<>();
		if (query.q() != null && !query.q().isBlank()) {
			must.add(Map.of("simple_query_string", Map.of(
					"query", query.q(),
					"fields", List.of("title^3", "content", "aiSummary", "suggestedAction", "category^2"),
					"default_operator", "and"
			)));
		}
		
		return Map.of(
				"from", query.page() * query.size(),
				"size", query.size(),
				"query", Map.of("bool", Map.of("filter", filter, "must", must)),
				"sort", List.of(Map.of("createdAt", Map.of("order", "desc")))
		);
	}
	
	private static void addTerm(List<Map<String, Object>> filter, String fieldName, Enum<?> value) {
		if (value != null) {
			filter.add(term(fieldName, value.name()));
		}
	}
	
	private static Map<String, Object> term(String fieldName, String value) {
		return Map.of("term", Map.of(fieldName, value));
	}
	
	private static Map<String, Object> mapping() {
		Map<String, Object> keyword = Map.of("type", "keyword");
		Map<String, Object> date = Map.of("type", "date");
		return Map.of("mappings", Map.of("properties", Map.ofEntries(
				Map.entry("id", keyword),
				Map.entry("tenantId", keyword),
				Map.entry("customerId", keyword),
				Map.entry("source", keyword),
				Map.entry("title", Map.of("type", "text")),
				Map.entry("content", Map.of("type", "text")),
				Map.entry("status", keyword),
				Map.entry("priority", keyword),
				Map.entry("sentiment", keyword),
				Map.entry("category", keyword),
				Map.entry("riskLevel", keyword),
				Map.entry("aiSummary", Map.of("type", "text")),
				Map.entry("suggestedAction", Map.of("type", "text")),
				Map.entry("createdAt", date),
				Map.entry("updatedAt", date)
		)));
	}
}
