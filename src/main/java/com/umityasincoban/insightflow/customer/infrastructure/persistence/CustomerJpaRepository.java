package com.umityasincoban.insightflow.customer.infrastructure.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
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
	
	@Query("""
			select c from CustomerEntity c
			where c.tenantId = :tenantId
			  and (
			    lower(coalesce(c.externalId, '')) like lower(concat('%', :query, '%'))
			    or lower(coalesce(c.email, '')) like lower(concat('%', :query, '%'))
			    or lower(coalesce(c.fullName, '')) like lower(concat('%', :query, '%'))
			    or lower(coalesce(c.plan, '')) like lower(concat('%', :query, '%'))
			  )
			""")
	Page<CustomerEntity> searchByTenantId(UUID tenantId, String query, Pageable pageable);
}
