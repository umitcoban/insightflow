package com.umityasincoban.insightflow.tenancy.infrastructure.persistence;

import com.umityasincoban.insightflow.tenancy.domain.TenantStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TenantJpaRepository extends JpaRepository<TenantEntity, UUID> {
	Optional<TenantEntity> findBySlug(String slug);
	
	boolean existsBySlug(String slug);
	
	List<TenantEntity> findByStatusOrderByCreatedAtDesc(TenantStatus status);
}
