package com.umityasincoban.insightflow.shared.security;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "insightflow.security.jwt")
public class InsightFlowSecurityProperties {
	
	@NotBlank
	private String issuerUri = "http://localhost:8081/realms/insightflow";
	
	@NotBlank
	private String audience = "insightflow-api";
	
	private String jwkSetUri;
	
	@NotBlank
	private String tenantIdClaim = "tenant_id";
	
	@NotBlank
	private String tenantSlugClaim = "tenant_slug";
	
	public String getIssuerUri() {
		return issuerUri;
	}
	
	public void setIssuerUri(String issuerUri) {
		this.issuerUri = issuerUri;
	}
	
	public String getAudience() {
		return audience;
	}
	
	public void setAudience(String audience) {
		this.audience = audience;
	}
	
	public String getJwkSetUri() {
		return jwkSetUri;
	}
	
	public void setJwkSetUri(String jwkSetUri) {
		this.jwkSetUri = jwkSetUri;
	}
	
	public String effectiveJwkSetUri() {
		if (jwkSetUri != null && !jwkSetUri.isBlank()) {
			return jwkSetUri;
		}
		return issuerUri.replaceAll("/+$", "") + "/protocol/openid-connect/certs";
	}
	
	public String getTenantIdClaim() {
		return tenantIdClaim;
	}
	
	public void setTenantIdClaim(String tenantIdClaim) {
		this.tenantIdClaim = tenantIdClaim;
	}
	
	public String getTenantSlugClaim() {
		return tenantSlugClaim;
	}
	
	public void setTenantSlugClaim(String tenantSlugClaim) {
		this.tenantSlugClaim = tenantSlugClaim;
	}
}
