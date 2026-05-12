package com.umityasincoban.insightflow.customer.domain;

import java.util.Objects;
import java.util.UUID;

public record CustomerId(UUID value) {
	
	public CustomerId {
		Objects.requireNonNull(value, "Customer id cannot be null");
	}
	
	public static CustomerId of(UUID value) {
		return new CustomerId(value);
	}
}