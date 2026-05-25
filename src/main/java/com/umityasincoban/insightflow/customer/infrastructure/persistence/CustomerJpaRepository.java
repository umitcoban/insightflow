package com.umityasincoban.insightflow.customer.infrastructure.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface CustomerJpaRepository extends JpaRepository<CustomerEntity, UUID> {
	
	Optional<CustomerEntity> findByTenantIdAndId(UUID tenantId, UUID id);
	
	Optional<CustomerEntity> findByTenantIdAndExternalId(UUID tenantId, String externalId);
	
	Optional<CustomerEntity> findByTenantIdAndEmail(UUID tenantId, String email);
	
	boolean existsByTenantIdAndId(UUID tenantId, UUID id);
	
	boolean existsByTenantIdAndExternalId(UUID tenantId, String externalId);
	
	boolean existsByTenantIdAndEmail(UUID tenantId, String email);
	
	Page<CustomerEntity> findByTenantId(UUID tenantId, Pageable pageable);
}