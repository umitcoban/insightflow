package com.umityasincoban.insightflow.search.infrastructure.elasticsearch;

import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Component
public class ElasticsearchRestClientFactory {
	
	private final ElasticsearchProperties properties;
	
	public ElasticsearchRestClientFactory(ElasticsearchProperties properties) {
		this.properties = properties;
	}
	
	public RestClient create() {
		RestClient.Builder builder = RestClient.builder().baseUrl(properties.getBaseUrl());
		if (hasText(properties.getUsername())) {
			String raw = properties.getUsername() + ":" + (properties.getPassword() == null ? "" : properties.getPassword());
			builder.defaultHeader(
					HttpHeaders.AUTHORIZATION,
					"Basic " + Base64.getEncoder().encodeToString(raw.getBytes(StandardCharsets.UTF_8))
			);
		}
		return builder.build();
	}
	
	private static boolean hasText(String value) {
		return value != null && !value.isBlank();
	}
}

