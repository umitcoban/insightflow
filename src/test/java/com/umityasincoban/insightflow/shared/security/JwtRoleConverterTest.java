package com.umityasincoban.insightflow.shared.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class JwtRoleConverterTest {
	
	private final JwtRoleConverter converter = new JwtRoleConverter();
	
	@Test
	void mapsPlatformAdminRole() {
		assertThat(converter.convert(jwtWithRoles("PLATFORM_ADMIN")))
				.extracting("authority")
				.containsExactly("ROLE_PLATFORM_ADMIN");
	}
	
	@Test
	void mapsTenantAdminRole() {
		assertThat(converter.convert(jwtWithRoles("TENANT_ADMIN")))
				.extracting("authority")
				.containsExactly("ROLE_TENANT_ADMIN");
	}
	
	@Test
	void mapsSupportAgentRole() {
		assertThat(converter.convert(jwtWithRoles("SUPPORT_AGENT")))
				.extracting("authority")
				.containsExactly("ROLE_SUPPORT_AGENT");
	}
	
	@Test
	void ignoresUnrelatedRealmRoles() {
		assertThat(converter.convert(jwtWithRoles("default-roles-insightflow", "offline_access")))
				.isEmpty();
	}
	
	@Test
	void absentRealmAccessYieldsNoApplicationRoles() {
		Jwt jwt = Jwt.withTokenValue("token")
				.header("alg", "none")
				.subject("user-1")
				.build();
		
		assertThat(converter.convert(jwt)).isEmpty();
	}
	
	private static Jwt jwtWithRoles(String... roles) {
		return Jwt.withTokenValue("token")
				.header("alg", "none")
				.subject("user-1")
				.claim("realm_access", Map.of("roles", List.of(roles)))
				.build();
	}
}
