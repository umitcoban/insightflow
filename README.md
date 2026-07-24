# InsightFlow

InsightFlow is a multi-tenant AI-powered customer intelligence and automation backend platform.

It helps SaaS companies centralize customer feedback, enrich it with AI-generated insights, search feedback data with Elasticsearch, answer questions using tenant-specific RAG, and automate actions through a dynamic rule engine.

## Current Scope

This project is built as an educational backend architecture project.

The main goals are:

- Java 25 and modern Java features
- Spring Boot 4
- Domain-Driven Design
- Multi-tenancy
- PostgreSQL and database migration
- Database indexing strategies
- Kafka-based event-driven architecture
- Elasticsearch-powered search
- AI enrichment and RAG
- Dynamic rule engine
- Testcontainers-based integration testing
- Observability and production-oriented practices

## Implementation Status

| Area | Status |
| --- | --- |
| Multi-tenancy | Implemented |
| Customer API | Implemented |
| Feedback API | Implemented |
| Keycloak/JWT security | Implemented |
| Transactional outbox | Implemented |
| Kafka event publishing/consuming | Implemented |
| Mock AI enrichment | Implemented |
| Automation Rule Engine v1 | Implemented |
| Elasticsearch feedback search | Planned |
| Real AI provider adapter | Planned |
| Tenant-specific RAG assistant | Planned |
| Production deployment hardening | Planned |

See `docs/finalization-roadmap.md` for the full finalization checklist, `docs/api-reference.md` for the current API surface, and `docs/demo-runbook.md` for an end-to-end local demo flow.

## Local Security

Local infrastructure runs through Docker Compose:

```bash
docker compose up -d
```

Compose starts PostgreSQL, Kafka, and Keycloak. Keycloak is exposed on `http://localhost:8081`, runs in development mode, and imports `docker/keycloak/insightflow-realm.json`.

Development-only Keycloak admin credentials:

- username: `admin`
- password: `admin`

Realm: `insightflow`

Clients:

- `insightflow-api`: backend API audience/resource identifier
- `insightflow-dev-client`: public development client with direct access grants enabled for manual token testing

Roles:

- `PLATFORM_ADMIN`
- `TENANT_ADMIN`
- `SUPPORT_AGENT`

Development users:

- `platform-admin` / `platform-admin`: `PLATFORM_ADMIN`
- `acme-admin` / `acme-admin`: `TENANT_ADMIN`
- `acme-agent` / `acme-agent`: `SUPPORT_AGENT`

The development Acme tenant is seeded by Flyway with UUID `11111111-1111-1111-1111-111111111111` and slug `acme`. Keycloak tokens for Acme users contain matching `tenant_id` and `tenant_slug` claims plus audience `insightflow-api`.

InsightFlow is a stateless OAuth2 Resource Server. JWT issuer defaults to `http://localhost:8081/realms/insightflow` and can be overridden with `INSIGHTFLOW_SECURITY_JWT_ISSUER_URI`. Tokens are validated for issuer, signature, timestamps, and audience.

Tenant users cannot choose a tenant through `X-Tenant-Slug`. For `TENANT_ADMIN` and `SUPPORT_AGENT`, the JWT tenant claims are the authorization boundary and `X-Tenant-Slug` must match `tenant_slug`. `PLATFORM_ADMIN` may access platform tenant administration without a tenant header and may select tenant-scoped context by sending `X-Tenant-Slug`.

Expected security failures use JSON ProblemDetail responses:

- `401 AUTHENTICATION_REQUIRED`: missing, invalid, or expired token
- `403 ACCESS_DENIED`: authenticated but role is insufficient
- `403 TENANT_ACCESS_DENIED`: tenant header/claim mismatch or missing tenant context

Use `http/auth.http` to obtain development tokens and call protected endpoints. The password grant/direct access flow is local-development only. A future frontend should use Authorization Code + PKCE.

Production must run Keycloak in production mode with a persistent supported database, real secrets, hardened TLS/proxy settings, and frontend CORS origins configured explicitly.
