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
	
	@PatchMapping("/{id}")
	public CustomerResponse updateCustomer(@PathVariable UUID id, @Valid @RequestBody UpdateCustomerRequest request) {
		return CustomerResponse.from(customerApplicationService.updateCustomer(
				id,
				request.externalId(),
				request.email(),
				request.fullName(),
				request.plan()
		));
	}
	
	@PostMapping("/{id}/deactivate")
	public CustomerResponse deactivateCustomer(@PathVariable UUID id) {
		return CustomerResponse.from(customerApplicationService.deactivateCustomer(id));
	}
	
	@PostMapping("/{id}/activate")
	public CustomerResponse activateCustomer(@PathVariable UUID id) {
		return CustomerResponse.from(customerApplicationService.activateCustomer(id));
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
	
	@GetMapping("/search")
	public PageResponse<CustomerResponse> searchCustomers(
			@RequestParam String q,
			@RequestParam(required = false) Integer page,
			@RequestParam(required = false) Integer size
	) {
		return PageResponse.from(customerApplicationService.searchCustomers(q, CustomerQuery.of(page, size)), CustomerResponse::from);
	}
}
