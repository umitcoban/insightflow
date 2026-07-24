package com.umityasincoban.insightflow.shared.tenancy;

import com.umityasincoban.insightflow.shared.security.AuthenticatedUser;
import com.umityasincoban.insightflow.shared.security.CurrentUserProvider;
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
	private final CurrentUserProvider currentUserProvider;
	
	public CurrentTenantProvider(TenantRepository tenantRepository, CurrentUserProvider currentUserProvider) {
		this.tenantRepository = tenantRepository;
		this.currentUserProvider = currentUserProvider;
	}
	
	@Transactional(readOnly = true)
	public Tenant getCurrentTenant() {
		AuthenticatedUser user = currentUserProvider.getCurrentUser();
		String tenantSlug = TenantContext.getRequiredTenantSlug();
		
		if (user.tenantUser()) {
			Tenant tenant = tenantRepository.findById(user.tenantId())
					.or(() -> tenantRepository.findBySlug(user.tenantSlug()))
					.orElseThrow(() -> new TenantNotFoundException(user.tenantSlug()));
			
			if (!tenant.getSlug().equals(tenantSlug) || !tenant.getSlug().equals(user.tenantSlug())) {
				throw new TenantHeaderJwtMismatchException();
			}
			
			if (!tenant.isActive()) {
				throw new TenantInactiveException(tenant.getSlug());
			}
			
			return tenant;
		}
		
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
