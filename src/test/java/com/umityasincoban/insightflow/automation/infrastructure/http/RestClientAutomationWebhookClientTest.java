package com.umityasincoban.insightflow.automation.infrastructure.http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import com.umityasincoban.insightflow.automation.application.AutomationWebhookRequest;
import com.umityasincoban.insightflow.automation.application.AutomationWebhookRequestException;
import com.umityasincoban.insightflow.automation.application.AutomationWebhookResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RestClientAutomationWebhookClientTest {
	
	private HttpServer server;
	private final RestClientAutomationWebhookClient client = new RestClientAutomationWebhookClient();
	
	@AfterEach
	void stopServer() {
		if (server != null) {
			server.stop(0);
		}
	}
	
	@Test
	void sendsRequestAndReturnsTwoHundredResponse() throws IOException {
		startServer(exchange -> respond(exchange, 200, "ok"));
		
		AutomationWebhookResponse response = client.send(request("/ok", 500));
		
		assertEquals(200, response.httpStatus());
		assertEquals("ok", response.responseBody());
		assertTrue(response.durationMs() >= 0);
	}
	
	@Test
	void returnsFiveHundredResponseWithoutThrowing() throws IOException {
		startServer(exchange -> respond(exchange, 500, "failed"));
		
		AutomationWebhookResponse response = client.send(request("/failed", 500));
		
		assertEquals(500, response.httpStatus());
		assertEquals("failed", response.responseBody());
	}
	
	@Test
	void timeoutThrowsRequestExceptionWithTimeoutType() throws IOException {
		startServer(exchange -> {
			try {
				Thread.sleep(500);
			} catch (InterruptedException exception) {
				Thread.currentThread().interrupt();
			}
			respond(exchange, 200, "late");
		});
		
		AutomationWebhookRequestException exception = assertThrows(
				AutomationWebhookRequestException.class,
				() -> client.send(request("/slow", 100))
		);
		
		assertEquals("TIMEOUT", exception.getResultPayload().get("errorType"));
	}
	
	private void startServer(ExchangeHandler handler) throws IOException {
		server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
		server.createContext("/", handler::handle);
		server.start();
	}
	
	private AutomationWebhookRequest request(String path, int timeoutMs) {
		return new AutomationWebhookRequest(
				URI.create("http://127.0.0.1:" + server.getAddress().getPort() + path),
				"POST",
				Map.of("Content-Type", "application/json"),
				Map.of("hello", "world"),
				timeoutMs
		);
	}
	
	private static void respond(HttpExchange exchange, int status, String body) throws IOException {
		byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
		exchange.sendResponseHeaders(status, bytes.length);
		exchange.getResponseBody().write(bytes);
		exchange.close();
	}
	
	@FunctionalInterface
	private interface ExchangeHandler {
		
		void handle(HttpExchange exchange) throws IOException;
	}
}
