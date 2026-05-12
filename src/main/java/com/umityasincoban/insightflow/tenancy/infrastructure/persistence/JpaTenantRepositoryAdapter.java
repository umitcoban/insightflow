package com.umityasincoban.insightflow.tenancy.infrastructure.persistence;

import com.umityasincoban.insightflow.tenancy.domain.Tenant;
import com.umityasincoban.insightflow.tenancy.domain.TenantRepository;
import com.umityasincoban.insightflow.tenancy.domain.TenantStatus;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class JpaTenantRepositoryAdapter implements TenantRepository {
	
	private final TenantJpaRepository tenantJpaRepository;
	private final TenantPersistenceMapper tenantPersistenceMapper;
	
	public JpaTenantRepositoryAdapter(
			TenantJpaRepository tenantJpaRepository,
			TenantPersistenceMapper tenantPersistenceMapper
	) {
		this.tenantJpaRepository = tenantJpaRepository;
		this.tenantPersistenceMapper = tenantPersistenceMapper;
	}
	
	@Override
	public Tenant save(String slug, String name, TenantStatus status) {
		TenantEntity entity = new TenantEntity(slug, name, status);
		TenantEntity savedEntity = tenantJpaRepository.save(entity);
		
		return tenantPersistenceMapper.toDomain(savedEntity);
	}
	
	@Override
	public Optional<Tenant> findBySlug(String slug) {
		return tenantJpaRepository.findBySlug(slug)
				.map(tenantPersistenceMapper::toDomain);
	}
	
	@Override
	public boolean existsBySlug(String slug) {
		return tenantJpaRepository.existsBySlug(slug);
	}
	
	@Override
	public List<Tenant> findAll() {
		return tenantJpaRepository.findAll()
				.stream()
				.map(tenantPersistenceMapper::toDomain)
				.toList();
	}
}
