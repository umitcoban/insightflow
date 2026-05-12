package com.umityasincoban.insightflow.shared.tenancy;

import com.umityasincoban.insightflow.tenancy.application.TenantInactiveException;
import com.umityasincoban.insightflow.tenancy.application.TenantNotFoundException;
import com.umityasincoban.insightflow.tenancy.domain.Tenant;
import com.umityasincoban.insightflow.tenancy.domain.TenantId;
import com.umityasincoban.insightflow.tenancy.domain.TenantRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class CurrentTenantProvider {
	
	private final TenantRepository tenantRepository;
	
	public CurrentTenantProvider(TenantRepository tenantRepository) {
		this.tenantRepository = tenantRepository;
	}
	
	@Transactional(readOnly = true)
	public Tenant getCurrentTenant() {
		String tenantSlug = TenantContext.getRequiredTenantSlug();
		
		Tenant tenant = tenantRepository.findBySlug(tenantSlug)
				.orElseThrow(() -> new TenantNotFoundException(tenantSlug));
		
		if (!tenant.isActive()) {
			throw new TenantInactiveException(tenantSlug);
		}
		
		return tenant;
	}
	
	@Transactional(readOnly = true)
	public TenantId getCurrentTenantId() {
		return getCurrentTenant().getId();
	}
}