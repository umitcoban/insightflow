# InsightFlow Agent Context

This document provides project context for AI coding agents such as Codex.

Before implementing any task, read this file carefully and follow the architectural conventions, technology choices, package structure, and current implementation status described here.

## Project Overview

InsightFlow is a multi-tenant, AI-powered customer intelligence and automation backend platform.

The product helps SaaS companies centralize customer feedback, enrich feedback with AI-generated insights, publish domain events through Kafka, and later automate operational actions through a dynamic rule engine.

The project is intentionally built as an educational backend architecture project. The goal is not only to ship features, but also to learn modern backend architecture by implementing real-world patterns step by step.

## Core Product Problem

Companies receive customer feedback, support requests, product complaints, app reviews, and bug reports from many different channels.

This data is often scattered, manually reviewed, difficult to search, and not automatically converted into operational actions.

InsightFlow solves this by:

* Collecting feedback per tenant.
* Enriching feedback with AI-generated sentiment, category, risk level, summary, and suggested action.
* Publishing domain events through a transactional outbox and Kafka.
* Supporting tenant-scoped data isolation.
* Preparing a dynamic rule engine so tenants can define “if this happens, then do that” automation flows.
* Preparing future Elasticsearch-based search and RAG-based knowledge assistant features.

## Main Architecture Style

The project follows a DDD-inspired modular monolith architecture.

The code is organized by business capability instead of purely technical layers.

Examples:

```text
tenancy/
  api/
  application/
  domain/
  infrastructure/persistence/

customer/
  api/
  application/
  domain/
  infrastructure/persistence/

feedback/
  api/
  application/
  domain/
  infrastructure/persistence/
  infrastructure/messaging/

outbox/
  application/
  domain/
  infrastructure/persistence/

ai/
  application/
  domain/
  infrastructure/

automation/
  domain/
  infrastructure/persistence/
```

The preferred dependency direction is:

```text
api -> application -> domain
infrastructure -> domain
application -> domain ports
```

Avoid this direction:

```text
domain -> infrastructure
application -> infrastructure implementation classes
```

When persistence is needed, define a domain/application port first, then implement it with a JPA adapter.

Example:

```text
FeedbackRepository          -> domain port
JpaFeedbackRepositoryAdapter -> infrastructure adapter
FeedbackJpaRepository        -> Spring Data JPA repository
FeedbackEntity               -> JPA entity
```

## Technology Stack

Current main stack:

* Java 25
* Spring Boot 4
* Gradle Kotlin DSL
* PostgreSQL 18
* Flyway
* Spring Data JPA
* Spring Security
* OAuth2 Resource Server dependency is present for future JWT-based security
* Spring Kafka
* Apache Kafka 4.x in Docker using KRaft mode
* MapStruct
* Testcontainers dependency is present for future integration tests
* Docker Compose for local infrastructure
* Jackson 3 through Spring Boot 4

Important Spring Boot 4 / Jackson 3 note:

Use Jackson 3 packages:

```java
import tools.jackson.databind.ObjectMapper;
import tools.jackson.core.JacksonException;
```

Do not use old Jackson 2 packages unless there is a very specific reason:

```java
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
```

## Local Infrastructure Policy

Do not require local installation of infrastructure services.

The user does not want to install PostgreSQL, Kafka, Elasticsearch, or similar services directly on the host machine.

All infrastructure should run through Docker Compose or Testcontainers.

Local machine should only need:

* JDK 25
* IntelliJ IDEA
* Docker Desktop
* Git

## Current Docker Compose Services

Current Docker Compose includes:

* PostgreSQL
* Kafka

PostgreSQL runs as:

```text
container: insightflow-postgres
database: insightflow
username: insightflow
password: insightflow
port: 5432
```

Kafka runs as:

```text
container: insightflow-kafka
bootstrap server from host: localhost:9092
domain events topic: insightflow.domain-events
```

Kafka uses Apache Kafka Docker image in KRaft mode.

## Database and Migration Strategy

Flyway is used for schema migrations.

Hibernate schema generation is disabled for schema creation. The application should validate schema, not create it.

Current JPA setting:

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: validate
    open-in-view: false
```

Migration files already implemented:

```text
V1__init_schema.sql
V2__create_tenants_table.sql
V3__create_customers_and_feedbacks_tables.sql
V4__enforce_feedback_customer_tenant_integrity.sql
V5__create_outbox_events_table.sql
V6__create_automation_rules_tables.sql
```

Database design intentionally uses:

* UUID primary keys
* timestamptz audit fields
* tenant_id discriminator for tenant-scoped data
* composite indexes starting with tenant_id
* check constraints for stable enum-like statuses
* JSONB fields for dynamic metadata, rule conditions, actions, and event payloads
* GIN indexes for JSONB where useful
* composite foreign key for feedback/customer tenant integrity

Important multi-tenant database rule:

When a child entity references another tenant-scoped entity, do not rely only on a simple foreign key.

For example, feedback/customer ownership is enforced through:

```text
feedbacks (tenant_id, customer_id)
references customers (tenant_id, id)
```

This prevents a feedback from tenant A from referencing a customer of tenant B.

## Multi-Tenancy Model

The project currently uses:

```text
Shared database + shared schema + tenant_id discriminator
```

Tenant resolution currently happens through request header:

```http
X-Tenant-Slug: acme
```

The request filter stores the current tenant slug in `TenantContext`.

`CurrentTenantProvider` resolves the current tenant slug into a real `TenantId` by querying the database.

Tenant-scoped endpoints must use `CurrentTenantProvider` instead of reading headers directly.

Important classes:

```text
TenantContext
TenantContextFilter
TenantHeaders
CurrentTenantProvider
TenantNotResolvedException
TenantInactiveException
```

Tenant-scoped endpoints must fail when tenant is missing or invalid.

Expected error codes:

```text
TENANT_NOT_RESOLVED
TENANT_NOT_FOUND
TENANT_INACTIVE
```

Current tenant endpoint behavior:

* `/api/v1/tenants/**` is platform-level for now.
* `/api/v1/customers/**` is tenant-scoped.
* `/api/v1/feedbacks/**` is tenant-scoped.
* Future automation/rule endpoints should be tenant-scoped.

## Observability

The project has correlation ID support.

Request header:

```http
X-Correlation-Id: demo-id
```

If missing, the backend generates one.

Important classes:

```text
CorrelationId
CorrelationIdFilter
RequestLoggingFilter
```

The logging pattern includes:

```text
correlationId
tenantSlug
```

Request logs include:

```text
method
uri
status
durationMs
```

Avoid logging request bodies, access tokens, authorization headers, or sensitive personal data.

## Error Handling

The project uses a global exception handler with Spring `ProblemDetail`.

Important class:

```text
GlobalExceptionHandler
```

Standard error responses include:

```text
type
title
status
detail
instance
errorCode
timestamp
correlationId
```

Current handled cases include:

* Tenant already exists
* Tenant not found
* Tenant inactive
* Tenant not resolved
* Customer already exists
* Customer not found
* Feedback not found
* Validation errors
* Invalid request parameters
* Invalid request body
* Unexpected errors

When adding new application exceptions, add a specific handler to `GlobalExceptionHandler`.

Do not return raw stack traces or internal exception messages to API clients.

## Security Status

Spring Security is present.

Current `SecurityConfig` temporarily permits API endpoints during development.

Currently permitted paths include:

```text
/actuator/health
/api/v1/tenants/**
/api/v1/customers/**
/api/v1/feedbacks/**
```

When adding new endpoints during development, temporarily permit them if needed.

Future work:

* JWT Resource Server
* tenant claim from JWT
* role-based authorization
* platform admin vs tenant admin vs support agent permissions

## Implemented Domain Areas

### Tenancy

Implemented:

* Tenant table
* TenantStatus
* TenantId
* Tenant domain model
* TenantEntity
* TenantJpaRepository
* TenantRepository port
* JpaTenantRepositoryAdapter
* TenantPersistenceMapper
* TenantApplicationService
* TenantController
* Create/list/get tenant endpoints

Tenant status values:

```text
ACTIVE
SUSPENDED
```

### Customer

Implemented:

* Customer table
* CustomerId
* Customer domain model
* CustomerEntity
* CustomerJpaRepository
* CustomerRepository port
* JpaCustomerRepositoryAdapter
* CustomerPersistenceMapper
* CustomerApplicationService
* CustomerController
* Customer create/list/get endpoints
* Tenant-scoped customer uniqueness checks

Customer API:

```http
POST /api/v1/customers
GET  /api/v1/customers?page=0&size=20
GET  /api/v1/customers/{id}
```

Customer is tenant-scoped and requires:

```http
X-Tenant-Slug
```

### Feedback

Implemented:

* Feedback table
* FeedbackId
* Feedback domain model
* FeedbackEntity
* FeedbackJpaRepository
* FeedbackRepository port
* JpaFeedbackRepositoryAdapter
* FeedbackPersistenceMapper
* FeedbackApplicationService
* FeedbackController
* Feedback create/list/get endpoints
* Pagination through `PageResponse`
* Query object through `FeedbackQuery`
* Tenant/customer ownership validation
* AI analysis update support

Feedback API:

```http
POST /api/v1/feedbacks
GET  /api/v1/feedbacks?page=0&size=20
GET  /api/v1/feedbacks?status=NEW&page=0&size=20
GET  /api/v1/feedbacks?priority=HIGH&page=0&size=20
GET  /api/v1/feedbacks/{id}
```

Feedback is tenant-scoped and requires:

```http
X-Tenant-Slug
```

Feedback enum values:

```text
FeedbackSource:
MANUAL
API
EMAIL
APP_REVIEW

FeedbackStatus:
NEW
IN_REVIEW
RESOLVED
ARCHIVED

FeedbackPriority:
LOW
MEDIUM
HIGH
CRITICAL

FeedbackSentiment:
POSITIVE
NEUTRAL
NEGATIVE

FeedbackRiskLevel:
LOW
MEDIUM
HIGH
CHURN_RISK
```

### Outbox

Implemented transactional outbox pattern.

Database table:

```text
outbox_events
```

Implemented:

* OutboxEventStatus
* OutboxEvent
* OutboxEventEntity
* OutboxEventJpaRepository
* OutboxEventRepository port
* JpaOutboxEventRepositoryAdapter
* OutboxEventPersistenceMapper
* OutboxPayloadFactory
* OutboxEventPublisher interface
* LoggingOutboxEventPublisher
* KafkaOutboxEventPublisher
* OutboxPublishingService
* OutboxPublishingScheduler
* OutboxRetryPolicy
* OutboxEventMessageSerializer
* OutboxEventMessageDeserializer
* OutboxEventMessage
* OutboxEventPublishException
* OutboxEventSerializationException
* OutboxEventDeserializationException

Outbox lifecycle:

```text
PENDING -> PUBLISHED
PENDING -> PENDING with next_retry_at when retryable failure occurs
PENDING -> FAILED when retry limit is exceeded
```

Outbox publisher uses:

```sql
FOR UPDATE SKIP LOCKED
```

This prevents multiple app instances from publishing the same pending event concurrently.

The scheduler delay is configured through:

```yaml
insightflow:
  outbox:
    publisher:
      fixed-delay-ms: 5000
```

### Kafka

Implemented:

* Kafka Docker service
* Spring Kafka producer config
* Kafka topic config
* Kafka publishing through outbox
* Kafka consumer for feedback.created

Topic:

```text
insightflow.domain-events
```

Current published domain events:

```text
feedback.created
feedback.ai-analysis-completed
```

Kafka message envelope:

```json
{
  "eventId": "...",
  "tenantId": "...",
  "aggregateType": "FEEDBACK",
  "aggregateId": "...",
  "eventType": "feedback.created",
  "eventVersion": 1,
  "payload": {},
  "createdAt": "..."
}
```

### AI Enrichment

Implemented mock AI enrichment.

Implemented:

* FeedbackAiAnalysisResult
* FeedbackAiAnalyzer port
* MockFeedbackAiAnalyzer
* FeedbackAiEnrichmentService

Current flow:

```text
feedback.created Kafka event
        ↓
FeedbackCreatedKafkaConsumer
        ↓
FeedbackAiEnrichmentService
        ↓
MockFeedbackAiAnalyzer
        ↓
feedbacks table updated
        ↓
feedback.ai-analysis-completed outbox event
        ↓
Kafka publish
```

Mock AI fills:

```text
sentiment
category
riskLevel
aiSummary
suggestedAction
```

Future real AI adapters may include:

* OpenAI
* Ollama
* Spring AI
* local LLM

### Automation / Rule Engine

Database migration exists.

Tables implemented:

```text
automation_rules
automation_executions
automation_action_executions
```

Already implemented Java persistence classes:

* AutomationRuleStatus
* AutomationExecutionStatus
* AutomationRuleEntity
* AutomationExecutionEntity
* AutomationActionExecutionEntity
* AutomationRuleJpaRepository
* AutomationExecutionJpaRepository
* AutomationActionExecutionJpaRepository

Current rule engine status:

Persistence entities exist, but rule API and rule evaluation are not implemented yet.

Next likely task:

Implement Automation Rule create/list API.

## Current HTTP Client Files

The project uses IntelliJ HTTP Client files.

Existing examples:

```text
http/tenants.http
http/customers.http
http/feedbacks.http
```

When adding new APIs, add a matching HTTP Client file.

For automation rules, create:

```text
http/automation-rules.http
```

## Current API Response Patterns

Use `PageResponse<T>` for paginated list endpoints.

Example:

```java
PageResponse.from(page, ResponseDto::from)
```

Do not return Spring Data `Page` directly from controllers.

Use DTO records for request/response models.

Do not expose JPA entities through REST APIs.

## Coding Style and Conventions

General:

* Prefer explicit constructors over Lombok.
* Lombok is intentionally not used for now.
* Use Java records for simple immutable value objects and request/response DTOs.
* Use classes for domain models that may gain behavior.
* Avoid public setters on JPA entities unless necessary.
* Prefer meaningful behavior methods such as `markAsPublished`, `applyAiAnalysis`, `activate`, `deactivate`.
* Use `OffsetDateTime` for audit timestamps.
* Use UUID identifiers.
* Use `@Transactional` from Spring, not Jakarta:

```java
import org.springframework.transaction.annotation.Transactional;
```

Do not use:

```java
import jakarta.transaction.Transactional;
```

because it does not support `readOnly`.

MapStruct:

* MapStruct is used.
* Default component model is configured as Spring through compiler args.
* Mapper interfaces usually do not need `componentModel = "spring"` explicitly.
* Add custom mapping methods for UUID to value object mappings.

Jackson:

* Spring Boot 4 uses Jackson 3.
* Use `tools.jackson.*` imports.

JPA:

* Use `@Enumerated(EnumType.STRING)` for enums.
* Use `@JdbcTypeCode(SqlTypes.JSON)` for JSONB fields.
* Avoid unnecessary `@ManyToOne` relations for tenant ownership fields.
* Prefer `UUID tenantId` for tenant-scoped ownership fields.
* Use tenant-aware repository methods such as `findByTenantIdAndId`.

## Important Architectural Rules

### Rule 1: Tenant isolation first

Any tenant-scoped data access must include tenant id.

Good:

```java
findByTenantIdAndId(tenantId, id)
```

Bad:

```java
findById(id)
```

unless it is platform-level data or a carefully justified internal operation.

### Rule 2: No entity leakage

Controllers should not return JPA entities.

Application services should ideally depend on domain ports, not JPA repositories directly.

### Rule 3: Use outbox for domain events

Do not publish Kafka messages directly from business transaction code.

Instead:

```text
business change + outbox event insert in same transaction
scheduler publishes outbox event to Kafka
```

### Rule 4: Add meaningful migrations

Do not rely on Hibernate DDL generation.

Schema changes must be Flyway migrations.

### Rule 5: Keep endpoints testable through IntelliJ HTTP Client

Every new API should have a sample request file under `http/`.

### Rule 6: Prefer incremental implementation

Do not implement huge features in one step.

Break tasks into small, reviewable changes.

## Next Planned Work

The next intended milestone is the Rule Engine v1.

Suggested task sequence:

1. Implement Automation Rule create/list API.
2. Implement rule condition DSL model.
3. Implement rule evaluator for `all` conditions.
4. Implement `LOG_ACTION` executor.
5. Add Kafka consumer handling `feedback.ai-analysis-completed`.
6. Load active rules by tenant and trigger event type.
7. Evaluate rules against event payload.
8. Record automation executions and action executions.
9. Add HTTP examples.
10. Add tests.

## Suggested First Codex Task

Implement Automation Rule create/list API.

Requirements:

* Read this file first.
* Follow existing architecture and style.
* Add `AutomationRule` domain model.
* Add `AutomationRuleId` value object.
* Add `AutomationRuleRepository` port.
* Add `JpaAutomationRuleRepositoryAdapter`.
* Add `AutomationRulePersistenceMapper` if needed.
* Add `AutomationRuleApplicationService`.
* Add request/response DTOs.
* Add `AutomationRuleController`.
* Add endpoints:

    * `POST /api/v1/automation/rules`
    * `GET /api/v1/automation/rules?page=0&size=20`
* Use `CurrentTenantProvider`.
* Require `X-Tenant-Slug`.
* Store condition JSON as `Map<String, Object>`.
* Store action JSON as `List<Map<String, Object>>`.
* Default rule status should be `ACTIVE`.
* Use existing `PageResponse` pattern.
* Add exception handling if needed.
* Temporarily permit `/api/v1/automation/rules/**` in `SecurityConfig`.
* Add `http/automation-rules.http`.
* Run `./gradlew clean build`.
* Do not implement rule evaluation yet.
* Do not implement action execution yet.
