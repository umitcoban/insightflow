package com.umityasincoban.insightflow.shared.observability;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
@Order(10)
public class CorrelationIdFilter extends OncePerRequestFilter {
	
	@Override
	protected void doFilterInternal(
			HttpServletRequest request,
			HttpServletResponse response,
			FilterChain filterChain
	) throws ServletException, IOException {
		String correlationId = resolveCorrelationId(request);
		
		try {
			MDC.put(CorrelationId.MDC_KEY, correlationId);
			response.setHeader(CorrelationId.HEADER_NAME, correlationId);
			
			filterChain.doFilter(request, response);
		} finally {
			MDC.remove(CorrelationId.MDC_KEY);
		}
	}
	
	private static String resolveCorrelationId(HttpServletRequest request) {
		String correlationId = request.getHeader(CorrelationId.HEADER_NAME);
		
		if (correlationId == null || correlationId.isBlank()) {
			return UUID.randomUUID().toString();
		}
		
		return correlationId.strip();
	}
	
	
}
