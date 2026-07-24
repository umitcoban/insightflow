package com.umityasincoban.insightflow.shared.security;

import com.umityasincoban.insightflow.automation.api.AutomationRuleController;
import com.umityasincoban.insightflow.automation.application.AutomationRuleApplicationService;
import com.umityasincoban.insightflow.automation.domain.AutomationRule;
import com.umityasincoban.insightflow.automation.domain.AutomationRuleId;
import com.umityasincoban.insightflow.automation.domain.AutomationRuleStatus;
import com.umityasincoban.insightflow.shared.config.SecurityConfig;
import com.umityasincoban.insightflow.shared.tenancy.TenantContextFilter;
import com.umityasincoban.insightflow.tenancy.api.TenantController;
import com.umityasincoban.insightflow.tenancy.application.TenantApplicationService;
import com.umityasincoban.insightflow.tenancy.domain.Tenant;
import com.umityasincoban.insightflow.tenancy.domain.TenantId;
import com.umityasincoban.insightflow.tenancy.domain.TenantRepository;
import com.umityasincoban.insightflow.tenancy.domain.TenantStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {
		TenantController.class,
		AutomationRuleController.class
})
@Import({
		SecurityConfig.class,
		TenantContextFilter.class,
		CurrentUserProvider.class,
		JsonAuthenticationEntryPoint.class,
		JsonAccessDeniedHandler.class,
		SecurityProblemDetailWriter.class
})
class SecurityConfigMockMvcTest {
	
	private static final UUID TENANT_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
	
	private final MockMvc mockMvc;
	
	@MockitoBean
	private TenantApplicationService tenantApplicationService;
	
	@MockitoBean
	private AutomationRuleApplicationService automationRuleApplicationService;
	
	@MockitoBean
	private TenantRepository tenantRepository;
	
	@MockitoBean
	private JwtDecoder jwtDecoder;
	
	@Autowired
	SecurityConfigMockMvcTest(MockMvc mockMvc) {
		this.mockMvc = mockMvc;
	}
	
	@Test
	void noTokenReturnsUnauthorized() throws Exception {
		mockMvc.perform(get("/api/v1/tenants"))
				.andExpect(status().isUnauthorized());
	}
	
	@Test
	void invalidRoleReturnsForbidden() throws Exception {
		mockMvc.perform(get("/api/v1/tenants").with(jwtForRole("OTHER_ROLE")))
				.andExpect(status().isForbidden());
	}
	
	@Test
	void platformAdminCanAccessTenantAdministration() throws Exception {
		when(tenantApplicationService.listTenants()).thenReturn(List.of(tenant()));
		
		mockMvc.perform(get("/api/v1/tenants").with(jwtForRole("PLATFORM_ADMIN")))
				.andExpect(status().isOk());
	}
	
	@Test
	void tenantAdminCannotAccessPlatformTenantAdministration() throws Exception {
		mockMvc.perform(get("/api/v1/tenants").with(tenantJwtForRole("TENANT_ADMIN")))
				.andExpect(status().isForbidden());
	}
	
	@Test
	void tenantAdminCanCreateAutomationRuleForOwnTenant() throws Exception {
		when(automationRuleApplicationService.createRule(anyString(), any(), anyString(), any(), any(), any()))
				.thenReturn(automationRule());
		
		mockMvc.perform(post("/api/v1/automation/rules")
						.with(tenantJwtForRole("TENANT_ADMIN"))
						.header("X-Tenant-Slug", "acme")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "name": "Rule",
								  "triggerEventType": "feedback.created",
								  "conditionJson": {},
								  "actionJson": [{"type":"LOG"}]
								}
								"""))
				.andExpect(status().isCreated());
	}
	
	@Test
	void supportAgentCanReadButCannotMutateRules() throws Exception {
		when(automationRuleApplicationService.listRules(any(), any()))
				.thenReturn(new PageImpl<>(List.of(automationRule())));
		
		mockMvc.perform(get("/api/v1/automation/rules")
						.with(tenantJwtForRole("SUPPORT_AGENT"))
						.header("X-Tenant-Slug", "acme"))
				.andExpect(status().isOk());
		
		mockMvc.perform(post("/api/v1/automation/rules")
						.with(tenantJwtForRole("SUPPORT_AGENT"))
						.header("X-Tenant-Slug", "acme")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{}"))
				.andExpect(status().isForbidden());
	}
	
	@Test
	void crossTenantHeaderMismatchReturnsForbidden() throws Exception {
		mockMvc.perform(get("/api/v1/automation/rules")
						.with(tenantJwtForRole("TENANT_ADMIN"))
						.header("X-Tenant-Slug", "other"))
				.andExpect(status().isForbidden());
	}
	
	@Test
	void missingTenantHeaderFailsForTenantScopedEndpoint() throws Exception {
		mockMvc.perform(get("/api/v1/automation/rules")
						.with(tenantJwtForRole("TENANT_ADMIN")))
				.andExpect(status().isForbidden());
	}
	
	@Test
	void platformAdminCanSelectTenantByHeader() throws Exception {
		when(tenantRepository.findBySlug("acme")).thenReturn(Optional.of(tenant()));
		when(automationRuleApplicationService.listRules(any(), any()))
				.thenReturn(new PageImpl<>(List.of(automationRule())));
		
		mockMvc.perform(get("/api/v1/automation/rules")
						.with(jwtForRole("PLATFORM_ADMIN"))
						.header("X-Tenant-Slug", "acme"))
				.andExpect(status().isOk());
	}
	
	private static Jwt jwtWithRoles(String... roles) {
		return Jwt.withTokenValue("token")
				.header("alg", "none")
				.subject("subject-1")
				.claim("preferred_username", "user")
				.claim("realm_access", Map.of("roles", List.of(roles)))
				.build();
	}
	
	private static RequestPostProcessor jwtForRole(String role) {
		return jwt()
				.jwt(jwtWithRoles(role))
				.authorities(new SimpleGrantedAuthority("ROLE_" + role));
	}
	
	private static Jwt tenantJwt(String role) {
		return Jwt.withTokenValue("token")
				.header("alg", "none")
				.subject("subject-1")
				.claim("preferred_username", "acme-user")
				.claim("tenant_id", TENANT_ID.toString())
				.claim("tenant_slug", "acme")
				.claim("realm_access", Map.of("roles", List.of(role)))
				.build();
	}
	
	private static RequestPostProcessor tenantJwtForRole(String role) {
		return jwt()
				.jwt(tenantJwt(role))
				.authorities(new SimpleGrantedAuthority("ROLE_" + role));
	}
	
	private static Tenant tenant() {
		OffsetDateTime now = OffsetDateTime.now();
		return new Tenant(TenantId.of(TENANT_ID), "acme", "Acme", TenantStatus.ACTIVE, now, now);
	}
	
	private static AutomationRule automationRule() {
		OffsetDateTime now = OffsetDateTime.now();
		return new AutomationRule(
				AutomationRuleId.of(UUID.randomUUID()),
				TenantId.of(TENANT_ID),
				"Rule",
				null,
				"feedback.created",
				Map.of(),
				List.of(Map.of("type", "LOG")),
				AutomationRuleStatus.ACTIVE,
				100,
				now,
				now
		);
	}
}
