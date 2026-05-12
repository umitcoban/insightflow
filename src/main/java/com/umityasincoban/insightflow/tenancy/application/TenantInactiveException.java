package com.umityasincoban.insightflow.tenancy.application;

public class TenantInactiveException extends RuntimeException {
	
	public TenantInactiveException(String slug) {
		super("Tenant is not active with slug: " + slug);
	}
}