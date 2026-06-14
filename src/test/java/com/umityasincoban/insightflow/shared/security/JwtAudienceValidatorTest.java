package com.umityasincoban.insightflow.shared.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class JwtAudienceValidatorTest {
	
	private final JwtAudienceValidator validator = new JwtAudienceValidator("insightflow-api");
	
	@Test
	void expectedAudienceIsAccepted() {
		assertThat(validator.validate(jwtWithAudience("insightflow-api")).hasErrors()).isFalse();
	}
	
	@Test
	void missingAudienceIsRejected() {
		assertThat(validator.validate(jwtWithAudience()).hasErrors()).isTrue();
	}
	
	@Test
	void wrongAudienceIsRejected() {
		assertThat(validator.validate(jwtWithAudience("other-api")).hasErrors()).isTrue();
	}
	
	private static Jwt jwtWithAudience(String... audience) {
		return Jwt.withTokenValue("token")
				.header("alg", "none")
				.subject("user-1")
				.audience(List.of(audience))
				.build();
	}
}
