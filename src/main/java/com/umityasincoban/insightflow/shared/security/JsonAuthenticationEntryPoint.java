package com.umityasincoban.insightflow.shared.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class JsonAuthenticationEntryPoint implements AuthenticationEntryPoint {
	
	private final SecurityProblemDetailWriter problemDetailWriter;
	
	public JsonAuthenticationEntryPoint(SecurityProblemDetailWriter problemDetailWriter) {
		this.problemDetailWriter = problemDetailWriter;
	}
	
	@Override
	public void commence(
			HttpServletRequest request,
			HttpServletResponse response,
			AuthenticationException authException
	) throws IOException {
		problemDetailWriter.write(
				request,
				response,
				HttpStatus.UNAUTHORIZED,
				"Authentication required",
				"Authentication is required to access this resource",
				"AUTHENTICATION_REQUIRED"
		);
	}
}
