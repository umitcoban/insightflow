package com.umityasincoban.insightflow.tenancy.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TenantRepository {
	
	Tenant save(String slug, String name, TenantStatus status);
	
	Optional<Tenant> findBySlug(String slug);
	
	Optional<Tenant> findById(UUID id);
	
	boolean existsBySlug(String slug);
	
	List<Tenant> findAll();
	
}
