package com.umityasincoban.insightflow.shared.tenancy;


import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.umityasincoban.insightflow.shared.security.AuthenticatedUser;
import com.umityasincoban.insightflow.shared.security.AuthenticatedUserNotFoundException;
import com.umityasincoban.insightflow.shared.security.CurrentUserProvider;
import com.umityasincoban.insightflow.shared.security.InvalidTenantClaimException;
import com.umityasincoban.insightflow.shared.security.JsonAccessDeniedHandler;
import com.umityasincoban.insightflow.shared.security.TenantClaimMissingException;
import com.umityasincoban.insightflow.tenancy.domain.Tenant;
import com.umityasincoban.insightflow.tenancy.domain.TenantRepository;
import org.slf4j.MDC;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class TenantContextFilter extends OncePerRequestFilter {
	
	private static final String MDC_TENANT_KEY = "tenantSlug";
	
	private final CurrentUserProvider currentUserProvider;
	private final TenantRepository tenantRepository;
	private final JsonAccessDeniedHandler accessDeniedHandler;
	
	public TenantContextFilter(
			CurrentUserProvider currentUserProvider,
			TenantRepository tenantRepository,
			JsonAccessDeniedHandler accessDeniedHandler
	) {
		this.currentUserProvider = currentUserProvider;
		this.tenantRepository = tenantRepository;
		this.accessDeniedHandler = accessDeniedHandler;
	}
	
	@Override
	protected void doFilterInternal(
			HttpServletRequest request,
			HttpServletResponse response,
			FilterChain filterChain
	) throws ServletException, IOException {
		try {
			resolveTenantContext(request);
			
			filterChain.doFilter(request, response);
		} catch (AccessDeniedException exception) {
			accessDeniedHandler.handle(request, response, exception);
		} finally {
			TenantContext.clear();
			MDC.remove(MDC_TENANT_KEY);
		}
	}
	
	private void resolveTenantContext(HttpServletRequest request) {
		if (!isTenantScopedRequest(request)) {
			return;
		}
		
		AuthenticatedUser user;
		try {
			user = currentUserProvider.getCurrentUser();
		} catch (AuthenticatedUserNotFoundException exception) {
			return;
		} catch (TenantClaimMissingException | InvalidTenantClaimException exception) {
			throw new TenantAccessDeniedException("Tenant JWT claims are missing or invalid");
		}
		
		String headerTenantSlug = normalizedHeaderTenantSlug(request);
		if (headerTenantSlug == null) {
			throw new TenantAccessDeniedException("Tenant header is required");
		}
		
		if (user.tenantUser()) {
			if (!headerTenantSlug.equals(user.tenantSlug())) {
				throw new TenantHeaderJwtMismatchException();
			}
			setTenantContext(user.tenantSlug());
			return;
		}
		
		if (user.platformAdmin()) {
			Tenant tenant = tenantRepository.findBySlug(headerTenantSlug)
					.orElseThrow(() -> new TenantAccessDeniedException("Tenant is not available"));
			if (!tenant.isActive()) {
				throw new TenantAccessDeniedException("Tenant is not available");
			}
			setTenantContext(tenant.getSlug());
		}
	}
	
	private static boolean isTenantScopedRequest(HttpServletRequest request) {
		String path = request.getRequestURI();
		return path.startsWith("/api/v1/customers")
				|| path.startsWith("/api/v1/feedbacks")
				|| path.startsWith("/api/v1/automation/rules")
				|| path.startsWith("/api/v1/automation/executions")
				|| path.startsWith("/api/v1/knowledge")
				|| path.startsWith("/api/v1/assistant");
	}
	
	private static String normalizedHeaderTenantSlug(HttpServletRequest request) {
		String tenantSlug = request.getHeader(TenantHeaders.TENANT_SLUG);
		return tenantSlug == null || tenantSlug.isBlank()
				? null
				: tenantSlug.strip().toLowerCase();
	}
	
	private static void setTenantContext(String tenantSlug) {
		TenantContext.setTenantSlug(tenantSlug);
		MDC.put(MDC_TENANT_KEY, tenantSlug);
	}
}
