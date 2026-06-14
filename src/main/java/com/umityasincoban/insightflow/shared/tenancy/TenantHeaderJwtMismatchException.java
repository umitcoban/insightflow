package com.umityasincoban.insightflow.shared.tenancy;

public class TenantHeaderJwtMismatchException extends TenantAccessDeniedException {
	public TenantHeaderJwtMismatchException() {
		super("Tenant header does not match JWT tenant claim");
	}
}
