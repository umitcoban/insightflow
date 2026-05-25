package com.umityasincoban.insightflow.customer.application;

public class CustomerAlreadyExistsException extends RuntimeException {
	
	public CustomerAlreadyExistsException(String reason) {
		super("Customer already exists: " + reason);
	}
}