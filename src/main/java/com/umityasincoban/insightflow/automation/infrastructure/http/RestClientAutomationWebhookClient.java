package com.umityasincoban.insightflow.automation.infrastructure.http;

import com.umityasincoban.insightflow.automation.application.AutomationWebhookClient;
import com.umityasincoban.insightflow.automation.application.AutomationWebhookRequest;
import com.umityasincoban.insightflow.automation.application.AutomationWebhookRequestException;
import com.umityasincoban.insightflow.automation.application.AutomationWebhookResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;

@Component
public class RestClientAutomationWebhookClient implements AutomationWebhookClient {
	
	@Override
	public AutomationWebhookResponse send(AutomationWebhookRequest request) {
		Instant startedAt = Instant.now();
		try {
			RestClient.RequestBodySpec requestSpec = restClient(request.timeoutMs())
					.method(HttpMethod.valueOf(request.method()))
					.uri(request.url())
					.headers(headers -> applyHeaders(headers, request.headers()));
			
			if ("GET".equals(request.method()) || request.body() == null) {
				return requestSpec.exchange((clientRequest, clientResponse) -> toResponse(clientResponse, startedAt));
			}
			
			return requestSpec.body(request.body())
					.exchange((clientRequest, clientResponse) -> toResponse(clientResponse, startedAt));
		} catch (ResourceAccessException exception) {
			throw requestException(errorType(exception), startedAt);
		} catch (RestClientException exception) {
			throw requestException("NETWORK_ERROR", startedAt);
		}
	}
	
	private static RestClient restClient(int timeoutMs) {
		NoRedirectSimpleClientHttpRequestFactory requestFactory = new NoRedirectSimpleClientHttpRequestFactory();
		Duration timeout = Duration.ofMillis(timeoutMs);
		requestFactory.setConnectTimeout(timeout);
		requestFactory.setReadTimeout(timeout);
		return RestClient.builder()
				.requestFactory(requestFactory)
				.build();
	}
	
	private static AutomationWebhookResponse toResponse(ClientHttpResponse response, Instant startedAt) throws IOException {
		String body = new String(response.getBody().readAllBytes(), StandardCharsets.UTF_8);
		return new AutomationWebhookResponse(
				response.getStatusCode().value(),
				body,
				durationMs(startedAt)
		);
	}
	
	private static void applyHeaders(HttpHeaders httpHeaders, Map<String, String> headers) {
		headers.forEach(httpHeaders::set);
	}
	
	private static AutomationWebhookRequestException requestException(String errorType, Instant startedAt) {
		return new AutomationWebhookRequestException(
				"Webhook request failed: " + errorType,
				Map.of(
						"success", false,
						"errorType", errorType,
						"durationMs", durationMs(startedAt)
				)
		);
	}
	
	private static String errorType(ResourceAccessException exception) {
		Throwable current = exception;
		while (current != null) {
			if (current instanceof SocketTimeoutException) {
				return "TIMEOUT";
			}
			current = current.getCause();
		}
		return "NETWORK_ERROR";
	}
	
	private static long durationMs(Instant startedAt) {
		return Math.max(0, Duration.between(startedAt, Instant.now()).toMillis());
	}
	
	private static final class NoRedirectSimpleClientHttpRequestFactory extends SimpleClientHttpRequestFactory {
		
		@Override
		protected void prepareConnection(HttpURLConnection connection, String httpMethod) throws IOException {
			super.prepareConnection(connection, httpMethod);
			connection.setInstanceFollowRedirects(false);
		}
	}
}
