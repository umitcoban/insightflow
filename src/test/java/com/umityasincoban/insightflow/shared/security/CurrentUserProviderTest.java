package com.umityasincoban.insightflow.shared.security;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CurrentUserProviderTest {
	
	private final CurrentUserProvider provider = new CurrentUserProvider(new InsightFlowSecurityProperties());
	
	@AfterEach
	void clearSecurityContext() {
		SecurityContextHolder.clearContext();
	}
	
	@Test
	void tenantUserClaimsAreParsed() {
		setAuthentication(jwt(
				Map.of(
						"preferred_username", "acme-admin",
						"tenant_id", "11111111-1111-1111-1111-111111111111",
						"tenant_slug", "acme",
						"realm_access", Map.of("roles", List.of("TENANT_ADMIN"))
				)
		));
		
		AuthenticatedUser user = provider.getCurrentUser();
		
		assertThat(user.username()).isEqualTo("acme-admin");
		assertThat(user.tenantId()).hasToString("11111111-1111-1111-1111-111111111111");
		assertThat(user.tenantSlug()).isEqualTo("acme");
		assertThat(user.roles()).containsExactly("TENANT_ADMIN");
	}
	
	@Test
	void invalidTenantUuidIsRejected() {
		setAuthentication(jwt(
				Map.of(
						"tenant_id", "not-a-uuid",
						"tenant_slug", "acme",
						"realm_access", Map.of("roles", List.of("TENANT_ADMIN"))
				)
		));
		
		assertThatThrownBy(provider::getCurrentUser)
				.isInstanceOf(InvalidTenantClaimException.class);
	}
	
	@Test
	void missingTenantClaimsAreRejectedForTenantUser() {
		setAuthentication(jwt(Map.of("realm_access", Map.of("roles", List.of("SUPPORT_AGENT")))));
		
		assertThatThrownBy(provider::getCurrentUser)
				.isInstanceOf(TenantClaimMissingException.class);
	}
	
	@Test
	void platformAdminCanOmitTenantClaims() {
		setAuthentication(jwt(Map.of("realm_access", Map.of("roles", List.of("PLATFORM_ADMIN")))));
		
		AuthenticatedUser user = provider.getCurrentUser();
		
		assertThat(user.platformAdmin()).isTrue();
		assertThat(user.tenantId()).isNull();
		assertThat(user.tenantSlug()).isNull();
	}
	
	private void setAuthentication(Jwt jwt) {
		JwtRoleConverter roleConverter = new JwtRoleConverter();
		SecurityContextHolder.getContext().setAuthentication(
				new JwtAuthenticationToken(jwt, roleConverter.convert(jwt))
		);
	}
	
	private static Jwt jwt(Map<String, Object> claims) {
		Jwt.Builder builder = Jwt.withTokenValue("token")
				.header("alg", "none")
				.subject("subject-1");
		claims.forEach(builder::claim);
		return builder.build();
	}
}
