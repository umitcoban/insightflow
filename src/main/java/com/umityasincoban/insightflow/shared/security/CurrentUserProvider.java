package com.umityasincoban.insightflow.shared.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class CurrentUserProvider {
	
	private final InsightFlowSecurityProperties securityProperties;
	
	public CurrentUserProvider(InsightFlowSecurityProperties securityProperties) {
		this.securityProperties = securityProperties;
	}
	
	public AuthenticatedUser getCurrentUser() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (!(authentication instanceof JwtAuthenticationToken jwtAuthentication) || !authentication.isAuthenticated()) {
			throw new AuthenticatedUserNotFoundException();
		}
		
		Jwt jwt = jwtAuthentication.getToken();
		Set<String> roles = authentication.getAuthorities()
				.stream()
				.map(GrantedAuthority::getAuthority)
				.filter(authority -> authority.startsWith("ROLE_"))
				.map(authority -> authority.substring("ROLE_".length()))
				.collect(Collectors.toUnmodifiableSet());
		
		UUID tenantId = parseOptionalTenantId(jwt);
		String tenantSlug = blankToNull(jwt.getClaimAsString(securityProperties.getTenantSlugClaim()));
		AuthenticatedUser user = new AuthenticatedUser(
				jwt.getSubject(),
				resolveUsername(jwt),
				tenantId,
				tenantSlug,
				roles
		);
		
		if (user.tenantUser()) {
			requireTenantClaims(user);
		}
		
		return user;
	}
	
	private UUID parseOptionalTenantId(Jwt jwt) {
		String tenantId = blankToNull(jwt.getClaimAsString(securityProperties.getTenantIdClaim()));
		if (tenantId == null) {
			return null;
		}
		
		try {
			return UUID.fromString(tenantId);
		} catch (IllegalArgumentException exception) {
			throw new InvalidTenantClaimException(securityProperties.getTenantIdClaim());
		}
	}
	
	private void requireTenantClaims(AuthenticatedUser user) {
		if (user.tenantId() == null) {
			throw new TenantClaimMissingException(securityProperties.getTenantIdClaim());
		}
		if (user.tenantSlug() == null) {
			throw new TenantClaimMissingException(securityProperties.getTenantSlugClaim());
		}
	}
	
	private static String resolveUsername(Jwt jwt) {
		String preferredUsername = blankToNull(jwt.getClaimAsString("preferred_username"));
		return preferredUsername == null ? jwt.getSubject() : preferredUsername;
	}
	
	private static String blankToNull(String value) {
		return value == null || value.isBlank() ? null : value.strip();
	}
}
