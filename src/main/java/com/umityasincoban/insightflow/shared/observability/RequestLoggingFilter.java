package com.umityasincoban.insightflow.shared.observability;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Order(30)
public class RequestLoggingFilter extends OncePerRequestFilter {
	
	private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);
	
	@Override
	protected void doFilterInternal(
			HttpServletRequest request,
			HttpServletResponse response,
			FilterChain filterChain
	) throws ServletException, IOException {
		long startTime = System.currentTimeMillis();
		
		try {
			filterChain.doFilter(request, response);
		} finally {
			long durationMs = System.currentTimeMillis() - startTime;
			
			log.info(
					"HTTP request completed method={} uri={} status={} durationMs={}",
					request.getMethod(),
					request.getRequestURI(),
					response.getStatus(),
					durationMs
			);
		}
	}
}