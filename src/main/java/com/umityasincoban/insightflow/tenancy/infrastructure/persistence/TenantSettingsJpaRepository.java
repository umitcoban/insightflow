package com.umityasincoban.insightflow.tenancy.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TenantSettingsJpaRepository extends JpaRepository<TenantSettingsEntity, UUID> {
}

