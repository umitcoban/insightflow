package com.umityasincoban.insightflow.customer.infrastructure.persistence;

import com.umityasincoban.insightflow.customer.domain.Customer;
import com.umityasincoban.insightflow.customer.domain.CustomerId;
import com.umityasincoban.insightflow.customer.domain.CustomerRepository;
import com.umityasincoban.insightflow.tenancy.domain.TenantId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class JpaCustomerRepositoryAdapter implements CustomerRepository {
	
	private final CustomerJpaRepository customerJpaRepository;
	private final CustomerPersistenceMapper customerPersistenceMapper;
	
	public JpaCustomerRepositoryAdapter(
			CustomerJpaRepository customerJpaRepository,
			CustomerPersistenceMapper customerPersistenceMapper
	) {
		this.customerJpaRepository = customerJpaRepository;
		this.customerPersistenceMapper = customerPersistenceMapper;
	}
	
	@Override
	public Customer saveNew(
			TenantId tenantId,
			String externalId,
			String email,
			String fullName,
			String plan
	) {
		CustomerEntity entity = new CustomerEntity(
				tenantId.value(),
				normalizeOptional(externalId),
				normalizeEmail(email),
				normalizeOptional(fullName),
				normalizeOptional(plan)
		);
		
		CustomerEntity savedEntity = customerJpaRepository.save(entity);
		
		return customerPersistenceMapper.toDomain(savedEntity);
	}
	
	@Override
	public Optional<Customer> findByTenantIdAndId(TenantId tenantId, CustomerId customerId) {
		return customerJpaRepository.findByTenantIdAndId(tenantId.value(), customerId.value())
				.map(customerPersistenceMapper::toDomain);
	}
	
	@Override
	public Optional<Customer> findByTenantIdAndExternalId(TenantId tenantId, String externalId) {
		return customerJpaRepository.findByTenantIdAndExternalId(tenantId.value(), normalizeOptional(externalId))
				.map(customerPersistenceMapper::toDomain);
	}
	
	@Override
	public Optional<Customer> findByTenantIdAndEmail(TenantId tenantId, String email) {
		return customerJpaRepository.findByTenantIdAndEmail(tenantId.value(), normalizeEmail(email))
				.map(customerPersistenceMapper::toDomain);
	}
	
	@Override
	public boolean existsByTenantIdAndId(TenantId tenantId, CustomerId customerId) {
		return customerJpaRepository.existsByTenantIdAndId(tenantId.value(), customerId.value());
	}
	
	@Override
	public boolean existsByTenantIdAndExternalId(TenantId tenantId, String externalId) {
		String normalizedExternalId = normalizeOptional(externalId);
		
		if (normalizedExternalId == null) {
			return false;
		}
		
		return customerJpaRepository.existsByTenantIdAndExternalId(tenantId.value(), normalizedExternalId);
	}
	
	@Override
	public boolean existsByTenantIdAndEmail(TenantId tenantId, String email) {
		String normalizedEmail = normalizeEmail(email);
		
		if (normalizedEmail == null) {
			return false;
		}
		
		return customerJpaRepository.existsByTenantIdAndEmail(tenantId.value(), normalizedEmail);
	}
	
	@Override
	public Page<Customer> findByTenantId(TenantId tenantId, Pageable pageable) {
		return customerJpaRepository.findByTenantId(tenantId.value(), pageable)
				.map(customerPersistenceMapper::toDomain);
	}
	
	private static String normalizeOptional(String value) {
		if (value == null || value.isBlank()) {
			return null;
		}
		
		return value.strip();
	}
	
	private static String normalizeEmail(String email) {
		if (email == null || email.isBlank()) {
			return null;
		}
		
		return email.strip().toLowerCase();
	}
}