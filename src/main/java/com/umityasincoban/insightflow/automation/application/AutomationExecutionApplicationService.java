package com.umityasincoban.insightflow.automation.application;

import com.umityasincoban.insightflow.automation.domain.AutomationExecution;
import com.umityasincoban.insightflow.automation.domain.AutomationExecutionRepository;
import com.umityasincoban.insightflow.shared.tenancy.CurrentTenantProvider;
import com.umityasincoban.insightflow.tenancy.domain.TenantId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AutomationExecutionApplicationService {
	
	private final AutomationExecutionRepository automationExecutionRepository;
	private final CurrentTenantProvider currentTenantProvider;
	
	public AutomationExecutionApplicationService(
			AutomationExecutionRepository automationExecutionRepository,
			CurrentTenantProvider currentTenantProvider
	) {
		this.automationExecutionRepository = automationExecutionRepository;
		this.currentTenantProvider = currentTenantProvider;
	}
	
	@Transactional(readOnly = true)
	public Page<AutomationExecution> listExecutions(Integer page, Integer size) {
		TenantId tenantId = currentTenantProvider.getCurrentTenantId();
		
		PageRequest pageRequest = PageRequest.of(
				normalizePage(page),
				normalizeSize(size),
				Sort.by(Sort.Direction.DESC, "startedAt")
		);
		
		return automationExecutionRepository.findByTenantId(tenantId, pageRequest);
	}
	
	private static int normalizePage(Integer page) {
		if (page == null || page < 0) {
			return 0;
		}
		
		return page;
	}
	
	private static int normalizeSize(Integer size) {
		if (size == null || size < 1) {
			return 20;
		}
		
		return Math.min(size, 100);
	}
}
