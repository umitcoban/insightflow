package com.umityasincoban.insightflow.shared.security;

public final class SecurityRoles {
	public static final String PLATFORM_ADMIN = "PLATFORM_ADMIN";
	public static final String TENANT_ADMIN = "TENANT_ADMIN";
	public static final String SUPPORT_AGENT = "SUPPORT_AGENT";
	
	public static final String ROLE_PLATFORM_ADMIN = "ROLE_" + PLATFORM_ADMIN;
	public static final String ROLE_TENANT_ADMIN = "ROLE_" + TENANT_ADMIN;
	public static final String ROLE_SUPPORT_AGENT = "ROLE_" + SUPPORT_AGENT;
	
	private SecurityRoles() {
	}
}
