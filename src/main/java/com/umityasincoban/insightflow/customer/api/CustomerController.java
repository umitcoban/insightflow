package com.umityasincoban.insightflow.customer.api;

import com.umityasincoban.insightflow.customer.application.CustomerApplicationService;
import com.umityasincoban.insightflow.customer.application.CustomerQuery;
import com.umityasincoban.insightflow.shared.api.PageResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/customers")
public class CustomerController {
	
	private final CustomerApplicationService customerApplicationService;
	
	public CustomerController(CustomerApplicationService customerApplicationService) {
		this.customerApplicationService = customerApplicationService;
	}
	
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public CustomerResponse createCustomer(@Valid @RequestBody CreateCustomerRequest request) {
		return CustomerResponse.from(
				customerApplicationService.createCustomer(
						request.externalId(),
						request.email(),
						request.fullName(),
						request.plan()
				)
		);
	}
	
	@GetMapping
	public PageResponse<CustomerResponse> listCustomers(
			@RequestParam(required = false) Integer page,
			@RequestParam(required = false) Integer size
	) {
		CustomerQuery query = CustomerQuery.of(page, size);
		
		return PageResponse.from(
				customerApplicationService.listCustomers(query),
				CustomerResponse::from
		);
	}
	
	@GetMapping("/{id}")
	public CustomerResponse getCustomerById(@PathVariable UUID id) {
		return CustomerResponse.from(
				customerApplicationService.getCustomerById(id)
		);
	}
}