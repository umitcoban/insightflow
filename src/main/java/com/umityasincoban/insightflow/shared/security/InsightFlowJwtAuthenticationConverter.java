package com.umityasincoban.insightflow.shared.security;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

public class InsightFlowJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {
	
	private final JwtRoleConverter roleConverter;
	
	public InsightFlowJwtAuthenticationConverter(JwtRoleConverter roleConverter) {
		this.roleConverter = roleConverter;
	}
	
	@Override
	public AbstractAuthenticationToken convert(Jwt jwt) {
		String principalName = jwt.getClaimAsString("preferred_username");
		if (principalName == null || principalName.isBlank()) {
			principalName = jwt.getSubject();
		}
		
		return new JwtAuthenticationToken(jwt, roleConverter.convert(jwt), principalName);
	}
}
