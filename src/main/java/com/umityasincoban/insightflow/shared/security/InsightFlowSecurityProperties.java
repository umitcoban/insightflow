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
