package com.umityasincoban.insightflow.customer.application;

import com.umityasincoban.insightflow.customer.domain.Customer;
import com.umityasincoban.insightflow.customer.domain.CustomerId;
import com.umityasincoban.insightflow.customer.domain.CustomerRepository;
import com.umityasincoban.insightflow.shared.tenancy.CurrentTenantProvider;
import com.umityasincoban.insightflow.tenancy.domain.TenantId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class CustomerApplicationService {
	
	private final CustomerRepository customerRepository;
	private final CurrentTenantProvider currentTenantProvider;
	
	public CustomerApplicationService(
			CustomerRepository customerRepository,
			CurrentTenantProvider currentTenantProvider
	) {
		this.customerRepository = customerRepository;
		this.currentTenantProvider = currentTenantProvider;
	}
	
	@Transactional
	public Customer createCustomer(
			String externalId,
			String email,
			String fullName,
			String plan
	) {
		TenantId tenantId = currentTenantProvider.getCurrentTenantId();
		
		if (customerRepository.existsByTenantIdAndExternalId(tenantId, externalId)) {
			throw new CustomerAlreadyExistsException("externalId=" + externalId);
		}
		
		if (customerRepository.existsByTenantIdAndEmail(tenantId, email)) {
			throw new CustomerAlreadyExistsException("email=" + email);
		}
		
		return customerRepository.saveNew(
				tenantId,
				externalId,
				email,
				fullName,
				plan
		);
	}
	
	@Transactional(readOnly = true)
	public Page<Customer> listCustomers(CustomerQuery query) {
		TenantId tenantId = currentTenantProvider.getCurrentTenantId();
		
		PageRequest pageRequest = PageRequest.of(
				query.page(),
				query.size(),
				Sort.by(Sort.Direction.DESC, "createdAt")
		);
		
		return customerRepository.findByTenantId(tenantId, pageRequest);
	}
	
	@Transactional(readOnly = true)
	public Customer getCustomerById(UUID customerId) {
		TenantId tenantId = currentTenantProvider.getCurrentTenantId();
		
		return customerRepository.findByTenantIdAndId(tenantId, CustomerId.of(customerId))
				.orElseThrow(() -> new CustomerNotFoundException(customerId));
	}
}