package com.umityasincoban.insightflow.shared.security;

public class TenantClaimMissingException extends RuntimeException {
	public TenantClaimMissingException(String claimName) {
		super("JWT tenant claim is missing: " + claimName);
	}
}
