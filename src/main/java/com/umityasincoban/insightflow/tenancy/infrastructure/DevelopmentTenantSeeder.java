package com.umityasincoban.insightflow.tenancy.infrastructure;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@Profile("local")
public class DevelopmentTenantSeeder implements ApplicationRunner {
	
	private static final String ACME_TENANT_ID = "11111111-1111-1111-1111-111111111111";
	
	private final JdbcTemplate jdbcTemplate;
	
	public DevelopmentTenantSeeder(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}
	
	@Override
	public void run(ApplicationArguments args) {
		int updatedById = jdbcTemplate.update("""
				update tenants
				set slug = 'acme',
				    name = 'Acme Development',
				    status = 'ACTIVE',
				    updated_at = now()
				where id = ?::uuid
				""", ACME_TENANT_ID);
		if (updatedById > 0) {
			return;
		}
		try {
			jdbcTemplate.update("""
					insert into tenants (id, slug, name, status, created_at, updated_at)
					values (?::uuid, 'acme', 'Acme Development', 'ACTIVE', now(), now())
					""", ACME_TENANT_ID);
		} catch (DuplicateKeyException ignored) {
			jdbcTemplate.update("""
					update tenants
					set name = 'Acme Development',
					    status = 'ACTIVE',
					    updated_at = now()
					where slug = 'acme'
					""");
		}
	}
}
