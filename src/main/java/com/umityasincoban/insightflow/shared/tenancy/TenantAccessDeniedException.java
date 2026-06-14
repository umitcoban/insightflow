package com.umityasincoban.insightflow.shared.tenancy;

import org.springframework.security.access.AccessDeniedException;

public class TenantAccessDeniedException extends AccessDeniedException {
	public TenantAccessDeniedException(String message) {
		super(message);
	}
}
