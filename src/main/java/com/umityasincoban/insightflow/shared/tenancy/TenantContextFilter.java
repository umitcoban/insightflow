package com.umityasincoban.insightflow.shared.tenancy;


import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Order(20)
public class TenantContextFilter extends OncePerRequestFilter {
	
	private static final String MDC_TENANT_KEY = "tenantSlug";
	
	@Override
	protected void doFilterInternal(
			HttpServletRequest request,
			HttpServletResponse response,
			FilterChain filterChain
	) throws ServletException, IOException {
		String tenantSlug = request.getHeader(TenantHeaders.TENANT_SLUG);
		
		try {
			if (tenantSlug != null && !tenantSlug.isBlank()) {
				String normalizedTenantSlug = tenantSlug.strip().toLowerCase();
				
				TenantContext.setTenantSlug(normalizedTenantSlug);
				MDC.put(MDC_TENANT_KEY, normalizedTenantSlug);
			}
			
			filterChain.doFilter(request, response);
		} finally {
			TenantContext.clear();
			MDC.remove(MDC_TENANT_KEY);
		}
	}
}
