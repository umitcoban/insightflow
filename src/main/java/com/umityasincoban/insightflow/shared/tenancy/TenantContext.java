package com.umityasincoban.insightflow.shared.tenancy;

public final class TenantContext {
	
	private static final ThreadLocal<String> CURRENT_TENANT_SLUG = new ThreadLocal<>();
	
	private TenantContext() {
	}
	
	public static void setTenantSlug(String tenantSlug) {
		CURRENT_TENANT_SLUG.set(tenantSlug);
	}
	
	public static String getTenantSlug() {
		return CURRENT_TENANT_SLUG.get();
	}
	
	public static String getRequiredTenantSlug() {
		String tenantSlug = CURRENT_TENANT_SLUG.get();
		
		if (tenantSlug == null || tenantSlug.isBlank()) {
			throw new TenantNotResolvedException();
		}
		
		return tenantSlug;
	}
	
	public static boolean hasTenant() {
		String tenantSlug = CURRENT_TENANT_SLUG.get();
		return tenantSlug != null && !tenantSlug.isBlank();
	}
	
	public static void clear() {
		CURRENT_TENANT_SLUG.remove();
	}
}