package com.umityasincoban.insightflow.shared.security;

public class InvalidTenantClaimException extends RuntimeException {
	public InvalidTenantClaimException(String claimName) {
		super("JWT tenant claim is invalid: " + claimName);
	}
}
