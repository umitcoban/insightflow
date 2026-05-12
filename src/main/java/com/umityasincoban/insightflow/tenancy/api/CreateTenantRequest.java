package com.umityasincoban.insightflow.tenancy.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateTenantRequest(
		@NotBlank(message = "Tenant slug is required")
		@Size(max = 80, message = "Tenant slug cannot be longer than 80 characters")
		@Pattern(
				regexp = "^[a-z0-9]+(?:-[a-z0-9]+)*$",
				message = "Tenant slug must contain only lowercase letters, numbers and hyphens"
		)
		String slug,
		
		@NotBlank(message = "Tenant name is required")
		@Size(max = 160, message = "Tenant name cannot be longer than 160 characters")
		String name
) {
}