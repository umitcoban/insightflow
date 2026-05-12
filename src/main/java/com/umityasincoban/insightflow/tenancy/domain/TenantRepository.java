package com.umityasincoban.insightflow.tenancy.domain;

import java.util.List;
import java.util.Optional;

public interface TenantRepository {
	
	Tenant save(String slug, String name, TenantStatus status);
	
	Optional<Tenant> findBySlug(String slug);
	
	boolean existsBySlug(String slug);
	
	List<Tenant> findAll();
	
}
