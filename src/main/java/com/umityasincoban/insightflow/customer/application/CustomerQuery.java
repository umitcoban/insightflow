package com.umityasincoban.insightflow.customer.application;

public record CustomerQuery(
		int page,
		int size
) {
	
	private static final int DEFAULT_PAGE = 0;
	private static final int DEFAULT_SIZE = 20;
	private static final int MAX_SIZE = 100;
	
	public CustomerQuery {
		if (page < 0) {
			page = DEFAULT_PAGE;
		}
		
		if (size <= 0) {
			size = DEFAULT_SIZE;
		}
		
		if (size > MAX_SIZE) {
			size = MAX_SIZE;
		}
	}
	
	public static CustomerQuery of(Integer page, Integer size) {
		return new CustomerQuery(
				page == null ? DEFAULT_PAGE : page,
				size == null ? DEFAULT_SIZE : size
		);
	}
}