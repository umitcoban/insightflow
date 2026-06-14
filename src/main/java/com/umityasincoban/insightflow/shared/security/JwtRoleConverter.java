package com.umityasincoban.insightflow.shared.security;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class JwtRoleConverter implements Converter<Jwt, Collection<GrantedAuthority>> {
	
	private static final Set<String> APPLICATION_ROLES = Set.of(
			SecurityRoles.PLATFORM_ADMIN,
			SecurityRoles.TENANT_ADMIN,
			SecurityRoles.SUPPORT_AGENT
	);
	
	@Override
	public Collection<GrantedAuthority> convert(Jwt jwt) {
		Object realmAccess = jwt.getClaim("realm_access");
		if (!(realmAccess instanceof Map<?, ?> realmAccessMap)) {
			return List.of();
		}
		
		Object roles = realmAccessMap.get("roles");
		if (!(roles instanceof Collection<?> roleValues)) {
			return List.of();
		}
		
		return roleValues.stream()
				.filter(String.class::isInstance)
				.map(String.class::cast)
				.filter(APPLICATION_ROLES::contains)
				.map(role -> new SimpleGrantedAuthority("ROLE_" + role))
				.map(GrantedAuthority.class::cast)
				.toList();
	}
}
