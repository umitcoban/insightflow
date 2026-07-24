package com.umityasincoban.insightflow.tenancy.infrastructure.persistence;

import com.umityasincoban.insightflow.tenancy.domain.Tenant;
import com.umityasincoban.insightflow.tenancy.domain.TenantRepository;
import com.umityasincoban.insightflow.tenancy.domain.TenantId;
import com.umityasincoban.insightflow.tenancy.domain.TenantSettings;
import com.umityasincoban.insightflow.tenancy.domain.TenantStatus;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Repository
public class JpaTenantRepositoryAdapter implements TenantRepository {
	
	private final TenantJpaRepository tenantJpaRepository;
	private final TenantSettingsJpaRepository tenantSettingsJpaRepository;
	private final TenantPersistenceMapper tenantPersistenceMapper;
	
	public JpaTenantRepositoryAdapter(
			TenantJpaRepository tenantJpaRepository,
			TenantSettingsJpaRepository tenantSettingsJpaRepository,
			TenantPersistenceMapper tenantPersistenceMapper
	) {
		this.tenantJpaRepository = tenantJpaRepository;
		this.tenantSettingsJpaRepository = tenantSettingsJpaRepository;
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
	public Optional<Tenant> findById(UUID id) {
		return tenantJpaRepository.findById(id)
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
	
	@Override
	public Tenant updateStatus(String slug, TenantStatus status) {
		TenantEntity entity = tenantJpaRepository.findBySlug(slug).orElseThrow();
		if (TenantStatus.ACTIVE.equals(status)) {
			entity.activate();
		} else {
			entity.suspend();
		}
		return tenantPersistenceMapper.toDomain(entity);
	}
	
	@Override
	public TenantSettings getSettings(TenantId tenantId) {
		return tenantSettingsJpaRepository.findById(tenantId.value())
				.map(JpaTenantRepositoryAdapter::toSettings)
				.orElseGet(() -> new TenantSettings(tenantId, Map.of(), null));
	}
	
	@Override
	public TenantSettings saveSettings(TenantId tenantId, Map<String, Object> settings) {
		TenantSettingsEntity entity = tenantSettingsJpaRepository.findById(tenantId.value())
				.orElseGet(() -> new TenantSettingsEntity(tenantId.value(), Map.of()));
		entity.replaceSettings(settings);
		return toSettings(tenantSettingsJpaRepository.save(entity));
	}
	
	private static TenantSettings toSettings(TenantSettingsEntity entity) {
		return new TenantSettings(TenantId.of(entity.getTenantId()), entity.getSettings(), entity.getUpdatedAt());
	}
}
