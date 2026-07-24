package com.umityasincoban.insightflow.shared.observability;

import com.umityasincoban.insightflow.search.infrastructure.elasticsearch.ElasticsearchRestClientFactory;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Component("elasticsearch")
public class ElasticsearchHealthIndicator implements HealthIndicator {
	
	private final RestClient restClient;
	
	public ElasticsearchHealthIndicator(ElasticsearchRestClientFactory restClientFactory) {
		this.restClient = restClientFactory.create();
	}
	
	@Override
	public Health health() {
		try {
			Map<String, Object> response = restClient.get()
					.uri("/_cluster/health")
					.retrieve()
					.body(Map.class);
			String status = response == null ? "unknown" : String.valueOf(response.get("status"));
			return Health.up().withDetail("status", status).build();
		} catch (RuntimeException exception) {
			return Health.down(exception).build();
		}
	}
}
