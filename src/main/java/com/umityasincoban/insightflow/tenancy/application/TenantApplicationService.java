package com.umityasincoban.insightflow.tenancy.application;

import com.umityasincoban.insightflow.tenancy.domain.Tenant;
import com.umityasincoban.insightflow.tenancy.domain.TenantId;
import com.umityasincoban.insightflow.tenancy.domain.TenantRepository;
import com.umityasincoban.insightflow.tenancy.domain.TenantSettings;
import com.umityasincoban.insightflow.tenancy.domain.TenantStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class TenantApplicationService {
	
	private final TenantRepository tenantRepository;
	
	public TenantApplicationService(TenantRepository tenantRepository) {
		this.tenantRepository = tenantRepository;
	}
	
	@Transactional
	public Tenant createTenant(String slug, String name) {
		String normalizedSlug = normalizeSlug(slug);
		
		if (tenantRepository.existsBySlug(normalizedSlug)) {
			throw new TenantAlreadyExistsException(normalizedSlug);
		}
		
		return tenantRepository.save(
				normalizedSlug,
				name.strip(),
				TenantStatus.ACTIVE
		);
	}
	
	@Transactional(readOnly = true)
	public List<Tenant> listTenants() {
		return tenantRepository.findAll();
	}
	
	@Transactional(readOnly = true)
	public Tenant getTenantBySlug(String slug) {
		String normalizedSlug = normalizeSlug(slug);
		
		return tenantRepository.findBySlug(normalizedSlug)
				.orElseThrow(() -> new TenantNotFoundException(normalizedSlug));
	}
	
	@Transactional
	public Tenant suspendTenant(String slug) {
		String normalizedSlug = normalizeSlug(slug);
		if (tenantRepository.findBySlug(normalizedSlug).isEmpty()) {
			throw new TenantNotFoundException(normalizedSlug);
		}
		return tenantRepository.updateStatus(normalizedSlug, TenantStatus.SUSPENDED);
	}
	
	@Transactional
	public Tenant reactivateTenant(String slug) {
		String normalizedSlug = normalizeSlug(slug);
		if (tenantRepository.findBySlug(normalizedSlug).isEmpty()) {
			throw new TenantNotFoundException(normalizedSlug);
		}
		return tenantRepository.updateStatus(normalizedSlug, TenantStatus.ACTIVE);
	}
	
	@Transactional(readOnly = true)
	public TenantSettings getTenantSettings(String slug) {
		Tenant tenant = getTenantBySlug(slug);
		return tenantRepository.getSettings(tenant.getId());
	}
	
	@Transactional
	public TenantSettings updateTenantSettings(String slug, Map<String, Object> settings) {
		Tenant tenant = getTenantBySlug(slug);
		return tenantRepository.saveSettings(TenantId.of(tenant.getId().value()), settings);
	}
	
	private static String normalizeSlug(String slug) {
		if (slug == null) {
			return null;
		}
		
		return slug.strip().toLowerCase();
	}
}
