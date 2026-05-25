package com.umityasincoban.insightflow.customer.api;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record CreateCustomerRequest(
		@Size(max = 120, message = "External id cannot be longer than 120 characters")
		String externalId,
		
		@Email(message = "Customer email must be valid")
		@Size(max = 254, message = "Customer email cannot be longer than 254 characters")
		String email,
		
		@Size(max = 180, message = "Full name cannot be longer than 180 characters")
		String fullName,
		
		@Size(max = 60, message = "Plan cannot be longer than 60 characters")
		String plan
) {
}