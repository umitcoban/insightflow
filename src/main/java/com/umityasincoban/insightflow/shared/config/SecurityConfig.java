package com.umityasincoban.insightflow.shared.config;

import com.umityasincoban.insightflow.shared.security.InsightFlowJwtAuthenticationConverter;
import com.umityasincoban.insightflow.shared.security.InsightFlowSecurityProperties;
import com.umityasincoban.insightflow.shared.security.JsonAccessDeniedHandler;
import com.umityasincoban.insightflow.shared.security.JsonAuthenticationEntryPoint;
import com.umityasincoban.insightflow.shared.security.JwtAudienceValidator;
import com.umityasincoban.insightflow.shared.security.JwtRoleConverter;
import com.umityasincoban.insightflow.shared.security.SecurityRoles;
import com.umityasincoban.insightflow.shared.tenancy.TenantContextFilter;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableConfigurationProperties(InsightFlowSecurityProperties.class)
public class SecurityConfig {
	
	@Bean
	SecurityFilterChain securityFilterChain(
			HttpSecurity http,
			TenantContextFilter tenantContextFilter,
			JsonAuthenticationEntryPoint authenticationEntryPoint,
			JsonAccessDeniedHandler accessDeniedHandler
	) throws Exception {
		return http
				.csrf(AbstractHttpConfigurer::disable)
				.formLogin(AbstractHttpConfigurer::disable)
				.httpBasic(AbstractHttpConfigurer::disable)
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.exceptionHandling(exceptions -> exceptions
						.authenticationEntryPoint(authenticationEntryPoint)
						.accessDeniedHandler(accessDeniedHandler)
				)
				.oauth2ResourceServer(oauth2 -> oauth2
						.authenticationEntryPoint(authenticationEntryPoint)
						.accessDeniedHandler(accessDeniedHandler)
						.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
				)
				.addFilterAfter(tenantContextFilter, BearerTokenAuthenticationFilter.class)
				.authorizeHttpRequests(auth -> auth
						.requestMatchers("/actuator/health", "/actuator/health/**").permitAll()
						.requestMatchers(org.springframework.http.HttpMethod.POST, "/api/v1/tenants").hasRole(SecurityRoles.PLATFORM_ADMIN)
						.requestMatchers(org.springframework.http.HttpMethod.GET, "/api/v1/tenants/**").hasRole(SecurityRoles.PLATFORM_ADMIN)
						.requestMatchers(org.springframework.http.HttpMethod.POST, "/api/v1/tenants/*/suspend").hasRole(SecurityRoles.PLATFORM_ADMIN)
						.requestMatchers(org.springframework.http.HttpMethod.POST, "/api/v1/tenants/*/reactivate").hasRole(SecurityRoles.PLATFORM_ADMIN)
						.requestMatchers(org.springframework.http.HttpMethod.PUT, "/api/v1/tenants/*/settings").hasRole(SecurityRoles.PLATFORM_ADMIN)
						.requestMatchers(org.springframework.http.HttpMethod.POST, "/api/v1/customers").hasAnyRole(SecurityRoles.TENANT_ADMIN, SecurityRoles.PLATFORM_ADMIN)
						.requestMatchers(org.springframework.http.HttpMethod.PATCH, "/api/v1/customers/*").hasAnyRole(SecurityRoles.TENANT_ADMIN, SecurityRoles.PLATFORM_ADMIN)
						.requestMatchers(org.springframework.http.HttpMethod.POST, "/api/v1/customers/*/activate").hasAnyRole(SecurityRoles.TENANT_ADMIN, SecurityRoles.PLATFORM_ADMIN)
						.requestMatchers(org.springframework.http.HttpMethod.POST, "/api/v1/customers/*/deactivate").hasAnyRole(SecurityRoles.TENANT_ADMIN, SecurityRoles.PLATFORM_ADMIN)
						.requestMatchers(org.springframework.http.HttpMethod.POST, "/api/v1/feedbacks").hasAnyRole(SecurityRoles.TENANT_ADMIN, SecurityRoles.PLATFORM_ADMIN)
						.requestMatchers(org.springframework.http.HttpMethod.PATCH, "/api/v1/feedbacks/*/status").hasAnyRole(SecurityRoles.TENANT_ADMIN, SecurityRoles.SUPPORT_AGENT, SecurityRoles.PLATFORM_ADMIN)
						.requestMatchers(org.springframework.http.HttpMethod.PATCH, "/api/v1/feedbacks/*/priority").hasAnyRole(SecurityRoles.TENANT_ADMIN, SecurityRoles.SUPPORT_AGENT, SecurityRoles.PLATFORM_ADMIN)
						.requestMatchers(org.springframework.http.HttpMethod.PATCH, "/api/v1/feedbacks/*/assignment").hasAnyRole(SecurityRoles.TENANT_ADMIN, SecurityRoles.SUPPORT_AGENT, SecurityRoles.PLATFORM_ADMIN)
						.requestMatchers(org.springframework.http.HttpMethod.POST, "/api/v1/feedbacks/*/archive").hasAnyRole(SecurityRoles.TENANT_ADMIN, SecurityRoles.SUPPORT_AGENT, SecurityRoles.PLATFORM_ADMIN)
						.requestMatchers(org.springframework.http.HttpMethod.POST, "/api/v1/feedbacks/*/restore").hasAnyRole(SecurityRoles.TENANT_ADMIN, SecurityRoles.SUPPORT_AGENT, SecurityRoles.PLATFORM_ADMIN)
						.requestMatchers(org.springframework.http.HttpMethod.POST, "/api/v1/feedbacks/*/notes").hasAnyRole(SecurityRoles.TENANT_ADMIN, SecurityRoles.SUPPORT_AGENT, SecurityRoles.PLATFORM_ADMIN)
						.requestMatchers(org.springframework.http.HttpMethod.POST, "/api/v1/automation/rules").hasAnyRole(SecurityRoles.TENANT_ADMIN, SecurityRoles.PLATFORM_ADMIN)
						.requestMatchers(org.springframework.http.HttpMethod.PATCH, "/api/v1/automation/rules/*").hasAnyRole(SecurityRoles.TENANT_ADMIN, SecurityRoles.PLATFORM_ADMIN)
						.requestMatchers(org.springframework.http.HttpMethod.POST, "/api/v1/automation/rules/*/activate").hasAnyRole(SecurityRoles.TENANT_ADMIN, SecurityRoles.PLATFORM_ADMIN)
						.requestMatchers(org.springframework.http.HttpMethod.POST, "/api/v1/automation/rules/*/deactivate").hasAnyRole(SecurityRoles.TENANT_ADMIN, SecurityRoles.PLATFORM_ADMIN)
						.requestMatchers(org.springframework.http.HttpMethod.POST, "/api/v1/automation/rules/*/dry-run").hasAnyRole(SecurityRoles.TENANT_ADMIN, SecurityRoles.PLATFORM_ADMIN)
						.requestMatchers(org.springframework.http.HttpMethod.POST, "/api/v1/automation/rules/*/replay").hasAnyRole(SecurityRoles.TENANT_ADMIN, SecurityRoles.PLATFORM_ADMIN)
						.requestMatchers(org.springframework.http.HttpMethod.POST, "/api/v1/knowledge/documents").hasAnyRole(SecurityRoles.TENANT_ADMIN, SecurityRoles.PLATFORM_ADMIN)
						.requestMatchers(org.springframework.http.HttpMethod.DELETE, "/api/v1/knowledge/documents/*").hasAnyRole(SecurityRoles.TENANT_ADMIN, SecurityRoles.PLATFORM_ADMIN)
						.requestMatchers(org.springframework.http.HttpMethod.GET, "/api/v1/customers/**").hasAnyRole(SecurityRoles.TENANT_ADMIN, SecurityRoles.SUPPORT_AGENT, SecurityRoles.PLATFORM_ADMIN)
						.requestMatchers(org.springframework.http.HttpMethod.GET, "/api/v1/feedbacks/**").hasAnyRole(SecurityRoles.TENANT_ADMIN, SecurityRoles.SUPPORT_AGENT, SecurityRoles.PLATFORM_ADMIN)
						.requestMatchers(org.springframework.http.HttpMethod.GET, "/api/v1/automation/rules/**").hasAnyRole(SecurityRoles.TENANT_ADMIN, SecurityRoles.SUPPORT_AGENT, SecurityRoles.PLATFORM_ADMIN)
						.requestMatchers(org.springframework.http.HttpMethod.GET, "/api/v1/automation/executions/**").hasAnyRole(SecurityRoles.TENANT_ADMIN, SecurityRoles.SUPPORT_AGENT, SecurityRoles.PLATFORM_ADMIN)
						.requestMatchers(org.springframework.http.HttpMethod.GET, "/api/v1/knowledge/documents/**").hasAnyRole(SecurityRoles.TENANT_ADMIN, SecurityRoles.SUPPORT_AGENT, SecurityRoles.PLATFORM_ADMIN)
						.requestMatchers(org.springframework.http.HttpMethod.POST, "/api/v1/assistant/questions").hasAnyRole(SecurityRoles.TENANT_ADMIN, SecurityRoles.SUPPORT_AGENT, SecurityRoles.PLATFORM_ADMIN)
						.requestMatchers("/api/**").authenticated()
						.anyRequest().denyAll()
				)
				.build();
	}
	
	@Bean
	InsightFlowJwtAuthenticationConverter jwtAuthenticationConverter() {
		return new InsightFlowJwtAuthenticationConverter(new JwtRoleConverter());
	}
	
	@Bean
	JwtDecoder jwtDecoder(InsightFlowSecurityProperties securityProperties) {
		NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder.withJwkSetUri(securityProperties.effectiveJwkSetUri()).build();
		OAuth2TokenValidator<Jwt> validator = new DelegatingOAuth2TokenValidator<>(
				JwtValidators.createDefaultWithIssuer(securityProperties.getIssuerUri()),
				new JwtAudienceValidator(securityProperties.getAudience())
		);
		jwtDecoder.setJwtValidator(validator);
		return jwtDecoder;
	}
	
	@Bean
	FilterRegistrationBean<TenantContextFilter> tenantContextFilterRegistration(TenantContextFilter filter) {
		FilterRegistrationBean<TenantContextFilter> registration = new FilterRegistrationBean<>(filter);
		registration.setEnabled(false);
		return registration;
	}
}
