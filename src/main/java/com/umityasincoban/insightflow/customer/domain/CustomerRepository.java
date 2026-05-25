package com.umityasincoban.insightflow.customer.domain;

import com.umityasincoban.insightflow.tenancy.domain.TenantId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface CustomerRepository {
	
	Customer saveNew(
			TenantId tenantId,
			String externalId,
			String email,
			String fullName,
			String plan
	);
	
	Optional<Customer> findByTenantIdAndId(TenantId tenantId, CustomerId customerId);
	
	Optional<Customer> findByTenantIdAndExternalId(TenantId tenantId, String externalId);
	
	Optional<Customer> findByTenantIdAndEmail(TenantId tenantId, String email);
	
	boolean existsByTenantIdAndId(TenantId tenantId, CustomerId customerId);
	
	boolean existsByTenantIdAndExternalId(TenantId tenantId, String externalId);
	
	boolean existsByTenantIdAndEmail(TenantId tenantId, String email);
	
	Page<Customer> findByTenantId(TenantId tenantId, Pageable pageable);
}