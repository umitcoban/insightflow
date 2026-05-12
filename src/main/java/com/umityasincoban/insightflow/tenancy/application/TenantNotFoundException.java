package com.umityasincoban.insightflow.tenancy.application;

public class TenantNotFoundException extends RuntimeException{
	public TenantNotFoundException(String slug) {
		super("Tenant with slug " + slug + " not found");
	}
}
