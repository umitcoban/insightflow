package com.umityasincoban.insightflow.shared.security;

import com.umityasincoban.insightflow.shared.observability.CorrelationId;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.time.OffsetDateTime;

@Component
public class SecurityProblemDetailWriter {
	
	private final ObjectMapper objectMapper;
	
	public SecurityProblemDetailWriter(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}
	
	public void write(
			HttpServletRequest request,
			HttpServletResponse response,
			HttpStatus status,
			String title,
			String detail,
			String errorCode
	) throws IOException {
		ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, detail);
		problemDetail.setTitle(title);
		problemDetail.setType(URI.create("https://insightflow.dev/problems/" + errorCode.toLowerCase().replace('_', '-')));
		problemDetail.setInstance(URI.create(request.getRequestURI()));
		problemDetail.setProperty("errorCode", errorCode);
		problemDetail.setProperty("timestamp", OffsetDateTime.now());
		problemDetail.setProperty("correlationId", MDC.get(CorrelationId.MDC_KEY));
		
		response.setStatus(status.value());
		response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
		objectMapper.writeValue(response.getOutputStream(), problemDetail);
	}
}
