package com.umityasincoban.insightflow.shared.tenancy;

public class TenantNotResolvedException extends RuntimeException {
	
	public TenantNotResolvedException() {
		super("Tenant could not be resolved for the current request");
	}
}