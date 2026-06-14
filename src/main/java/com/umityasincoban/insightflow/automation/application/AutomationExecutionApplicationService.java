package com.umityasincoban.insightflow.automation.application;

import com.umityasincoban.insightflow.automation.domain.AutomationActionExecution;
import com.umityasincoban.insightflow.automation.domain.AutomationActionExecutionRepository;
import com.umityasincoban.insightflow.automation.domain.AutomationExecution;
import com.umityasincoban.insightflow.automation.domain.AutomationExecutionId;
import com.umityasincoban.insightflow.automation.domain.AutomationExecutionRepository;
import com.umityasincoban.insightflow.shared.tenancy.CurrentTenantProvider;
import com.umityasincoban.insightflow.tenancy.domain.TenantId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class AutomationExecutionApplicationService {
	
	private final AutomationExecutionRepository automationExecutionRepository;
	private final AutomationActionExecutionRepository automationActionExecutionRepository;
	private final CurrentTenantProvider currentTenantProvider;
	
	public AutomationExecutionApplicationService(
			AutomationExecutionRepository automationExecutionRepository,
			AutomationActionExecutionRepository automationActionExecutionRepository,
			CurrentTenantProvider currentTenantProvider
	) {
		this.automationExecutionRepository = automationExecutionRepository;
		this.automationActionExecutionRepository = automationActionExecutionRepository;
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
	
	@Transactional(readOnly = true)
	public AutomationExecution getExecution(UUID executionId) {
		TenantId tenantId = currentTenantProvider.getCurrentTenantId();
		
		return automationExecutionRepository.findByTenantIdAndId(tenantId, AutomationExecutionId.of(executionId))
				.orElseThrow(() -> new AutomationExecutionNotFoundException(executionId));
	}
	
	@Transactional(readOnly = true)
	public List<AutomationActionExecution> listActionExecutions(UUID executionId) {
		TenantId tenantId = currentTenantProvider.getCurrentTenantId();
		AutomationExecutionId automationExecutionId = AutomationExecutionId.of(executionId);
		
		automationExecutionRepository.findByTenantIdAndId(tenantId, automationExecutionId)
				.orElseThrow(() -> new AutomationExecutionNotFoundException(executionId));
		
		return automationActionExecutionRepository.findByTenantIdAndExecutionIdOrderByCreatedAtAsc(
				tenantId,
				automationExecutionId
		);
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
