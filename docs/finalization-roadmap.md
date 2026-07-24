# InsightFlow Finalization Roadmap

This checklist captures the remaining implementation work needed to turn the current backend into a complete, reviewable MVP and then into a production-oriented product.

## Finalization Target

The current codebase already has the core backend loop:

- Tenant-scoped customer and feedback APIs.
- Keycloak-backed JWT resource server security.
- Transactional outbox publishing to Kafka.
- Feedback AI enrichment through a mock analyzer.
- Automation rule CRUD, rule evaluation, LOG and WEBHOOK actions, execution history, and webhook retry support.

The recommended finalization path is split into three levels:

- **P0: MVP final** - make the existing implemented product coherent, tested, documented, and demoable.
- **P1: Product complete** - implement the README-level promises that are still missing.
- **P2: Production hardening** - operational maturity, observability depth, deployment profiles, and scale concerns.

## P0 - MVP Final

- [x] Clean git/worktree state before new feature work.
  - The stale infrastructure-package `DuplicateAutomationExecutionException` index entry was removed; the domain package exception is the single source.
- [x] Keep Java 25 build execution explicit for local development.
  - Use `JAVA_HOME=C:\Users\implo\.jdks\openjdk-25.0.1` when running Gradle on this machine.
- [x] Update project documentation so implemented automation v1 is no longer described as future work.
- [ ] Add integration tests for Flyway + JPA mappings.
  - Tenant schema validation.
  - Customer tenant uniqueness.
  - Feedback/customer composite tenant integrity.
  - Automation rules, executions, and action executions JSONB mappings.
- [ ] Add integration tests for transactional outbox.
  - Feedback creation inserts an outbox event in the same transaction.
  - Pending outbox events are claimed with bounded batches.
  - Publish success marks events as `PUBLISHED`.
  - Retryable publish failure updates retry metadata.
  - Exhausted publish failure marks events as `FAILED`.
- [ ] Add integration tests for Kafka event consumers.
  - `feedback.created` triggers AI enrichment.
  - AI completion produces `feedback.ai-analysis-completed`.
  - Automation consumer evaluates active tenant rules for supported events.
  - Malformed messages are skipped without killing the listener.
- [ ] Add API tests for tenant-scoped controllers beyond security-only coverage.
  - Customer create/list/get.
  - Feedback create/list/get with filters.
  - Automation rule create/list/get/update/activate/deactivate.
  - Automation execution list/detail/actions.
- [x] Add validation and error tests for automation request payloads.
  - Missing rule name.
  - Missing trigger event type.
  - Empty action list.
  - Invalid JSON shapes for condition/action payloads.
  - Unsupported action types.
- [x] Add production-like application profiles.
  - `application-local.yaml` for Docker Compose defaults.
  - `application-test.yaml` for Testcontainers-friendly settings.
  - `application-prod.yaml` with environment-only secrets and no development defaults.
- [ ] Make local HTTP client files security-consistent.
  - Prefer token variables from `http/auth.http`.
  - Remove or clearly mark old unauthenticated examples.
- [x] Add basic API documentation.
  - Either OpenAPI via springdoc when Spring Boot 4 compatibility is confirmed, or a maintained Markdown API reference.
- [x] Add a demo runbook.
  - Start Docker Compose.
  - Obtain dev tokens.
  - Create/list customers.
  - Create feedback.
  - Observe AI enrichment.
  - Create automation rule.
  - Observe automation execution history.
- [x] Add README status table.
  - Implemented.
  - MVP final remaining.
  - Future roadmap.

## P1 - Product Complete

- [x] Replace or complement `MockFeedbackAiAnalyzer` with a real AI adapter.
  - Spring AI adapter.
  - Ollama as the default local/open-source backend.
  - OpenAI support remains optional through Spring AI configuration.
  - Provider selection through configuration.
  - Timeout, retry, and failure behavior.
  - Prompt/template versioning.
  - Tenant-safe request logging with no sensitive content leakage.
- [x] Implement Elasticsearch-powered feedback search.
  - Add Elasticsearch/OpenSearch service to Docker Compose.
  - Add feedback search index mapping.
  - Publish/search-index feedback events from outbox/Kafka.
  - Add tenant-filtered search endpoint.
  - Support filters for status, priority, sentiment, risk level, category, source, customer, and date range.
  - Add reindex/backfill job.
  - Add Testcontainers coverage.
- [x] Implement tenant-specific RAG assistant.
  - Define knowledge sources.
  - Add document/chunk persistence.
  - Add embeddings provider.
  - Add vector store strategy.
  - Add tenant-scoped retrieval.
  - Add answer endpoint.
  - Add citation/source metadata.
  - Add prompt-injection and tenant-isolation safeguards.
- [x] Expand feedback lifecycle features.
  - Update status.
  - Update priority.
  - Assign owner.
  - Add internal notes/comments.
  - Archive/restore.
  - Track lifecycle timestamps.
- [x] Expand customer lifecycle features.
  - Update customer.
  - Deactivate/delete policy.
  - Search/filter customers.
  - Customer detail with feedback summary.
- [x] Expand tenant administration.
  - Suspend/reactivate tenants.
  - Tenant settings.
  - Tenant-level AI/search/automation configuration.
- [x] Expand automation rule DSL.
  - Explicit `all`/`any` groups.
  - Operators: `eq`, `neq`, `in`, `notIn`, `contains`, `exists`, `gt`, `gte`, `lt`, `lte`.
  - Date/time comparisons.
  - Numeric coercion rules.
  - Rule condition validation at create/update time.
- [ ] Expand automation actions.
  - Email action.
  - Slack/Teams action.
  - Ticket creation action.
  - Internal feedback update action.
  - Secret-reference support for authenticated webhooks.
- [x] Add rule execution controls.
  - Dry-run endpoint.
  - Manual replay endpoint.
  - Rule execution rate limits.
  - Per-rule retry policy overrides.
- [ ] Add audit logging.
  - Tenant/user/action metadata.
  - Rule changes.
  - Feedback lifecycle changes.
  - Administrative actions.

## P2 - Production Hardening

- [x] Replace development Keycloak mode with production deployment guidance.
  - Persistent Keycloak database.
  - Real secrets.
  - TLS/proxy settings.
  - Explicit frontend CORS origins.
- [x] Add operational observability.
  - Prometheus metrics.
  - Outbox lag metrics.
  - Kafka consumer lag metrics.
  - Automation execution success/failure metrics.
  - AI latency/error metrics.
  - Search indexing lag metrics.
- [x] Add structured health/readiness checks.
  - PostgreSQL.
  - Kafka.
  - AI provider.
  - Search/vector store when added.
- [x] Add container image build.
  - JVM runtime image.
  - Non-root user.
  - Environment-driven config.
  - Healthcheck.
- [x] Add CI pipeline.
  - Compile.
  - Unit tests.
  - Integration tests.
  - Dependency vulnerability scan.
  - Docker build.
- [ ] Add database migration discipline.
  - Migration checksum expectations.
  - Backward-compatible migration policy.
  - Seed data separated from production migrations.
- [ ] Add data privacy controls.
  - PII minimization.
  - Redaction policies.
  - Retention/deletion policy.
  - Tenant data export/delete workflows.
- [ ] Add resilience controls.
  - Idempotency for public write endpoints where needed.
  - Rate limits.
  - Request size limits.
  - Circuit breakers/timeouts for outbound integrations.
  - Dead-letter handling for unrecoverable Kafka messages.

## Recommended Next Execution Order

1. Clean the stale git index/worktree entry.
2. Update docs and README status so future work is not confused with completed automation v1.
3. Add P0 integration tests around database, outbox, Kafka, and automation.
4. Add local/test/prod profile split.
5. Add API documentation and demo runbook.
6. Choose the first P1 product milestone: Elasticsearch search, real AI adapter, or automation DSL v2.

## Current Verification

Using the explicit Java 25 path below, the current test baseline passed:

```powershell
$env:JAVA_HOME='C:\Users\implo\.jdks\openjdk-25.0.1'
$env:PATH="$env:JAVA_HOME\bin;$env:PATH"
.\gradlew.bat test
```
