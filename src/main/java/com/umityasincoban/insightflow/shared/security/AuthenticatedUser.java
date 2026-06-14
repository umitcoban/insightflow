package com.umityasincoban.insightflow.shared.security;

import java.util.Set;
import java.util.UUID;

public record AuthenticatedUser(
		String subject,
		String username,
		UUID tenantId,
		String tenantSlug,
		Set<String> roles
) {
	public boolean platformAdmin() {
		return roles.contains(SecurityRoles.PLATFORM_ADMIN);
	}
	
	public boolean tenantUser() {
		return roles.contains(SecurityRoles.TENANT_ADMIN) || roles.contains(SecurityRoles.SUPPORT_AGENT);
	}
}
