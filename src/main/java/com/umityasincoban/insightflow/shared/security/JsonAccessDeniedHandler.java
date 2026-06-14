package com.umityasincoban.insightflow.shared.security;

import com.umityasincoban.insightflow.shared.tenancy.TenantAccessDeniedException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class JsonAccessDeniedHandler implements AccessDeniedHandler {
	
	private final SecurityProblemDetailWriter problemDetailWriter;
	
	public JsonAccessDeniedHandler(SecurityProblemDetailWriter problemDetailWriter) {
		this.problemDetailWriter = problemDetailWriter;
	}
	
	@Override
	public void handle(
			HttpServletRequest request,
			HttpServletResponse response,
			AccessDeniedException accessDeniedException
	) throws IOException {
		String errorCode = accessDeniedException instanceof TenantAccessDeniedException
				? "TENANT_ACCESS_DENIED"
				: "ACCESS_DENIED";
		
		problemDetailWriter.write(
				request,
				response,
				HttpStatus.FORBIDDEN,
				"Access denied",
				"Access is denied",
				errorCode
		);
	}
}
