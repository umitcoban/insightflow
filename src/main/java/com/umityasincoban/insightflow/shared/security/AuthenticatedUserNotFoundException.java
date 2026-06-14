package com.umityasincoban.insightflow.shared.security;

public class AuthenticatedUserNotFoundException extends RuntimeException {
	public AuthenticatedUserNotFoundException() {
		super("Authenticated user was not found");
	}
}
