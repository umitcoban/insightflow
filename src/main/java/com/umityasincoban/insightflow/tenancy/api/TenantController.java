package com.umityasincoban.insightflow.tenancy.api;

import com.umityasincoban.insightflow.tenancy.application.TenantApplicationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tenants")
public class TenantController {
	private final TenantApplicationService tenantApplicationService;
	
	public TenantController(TenantApplicationService tenantApplicationService) {
		this.tenantApplicationService = tenantApplicationService;
	}
	
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public TenantResponse createTenant(@Valid @RequestBody CreateTenantRequest request) {
		return TenantResponse.from(
				tenantApplicationService.createTenant(request.slug(), request.name())
		);
	}
	
	@GetMapping
	public List<TenantResponse> listTenants() {
		return tenantApplicationService.listTenants()
				.stream()
				.map(TenantResponse::from)
				.toList();
	}
	
	@GetMapping("/{slug}")
	public TenantResponse getTenantBySlug(@PathVariable String slug) {
		return TenantResponse.from(
				tenantApplicationService.getTenantBySlug(slug)
		);
	}
}
