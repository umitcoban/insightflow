# InsightFlow API Reference

Base URL for local development:

```text
http://localhost:8080
```

All business endpoints except `/actuator/health` require a bearer JWT. Tenant-scoped endpoints also require:

```http
X-Tenant-Slug: acme
```

For tenant users, `X-Tenant-Slug` must match the tenant claims in the JWT. `PLATFORM_ADMIN` can use tenant administration endpoints without a tenant header, and can select tenant context for tenant-scoped endpoints by sending `X-Tenant-Slug`.

## Common Pagination Response

Paginated endpoints return:

```json
{
  "content": [],
  "page": 0,
  "size": 20,
  "totalElements": 0,
  "totalPages": 0,
  "first": true,
  "last": true
}
```

## Tenants

Required role: `PLATFORM_ADMIN`.

### Create Tenant

```http
POST /api/v1/tenants
Content-Type: application/json
Authorization: Bearer <token>
```

```json
{
  "slug": "acme",
  "name": "Acme"
}
```

### List Tenants

```http
GET /api/v1/tenants
Authorization: Bearer <token>
```

### Get Tenant By Slug

```http
GET /api/v1/tenants/{slug}
Authorization: Bearer <token>
```

## Customers

Required roles:

- Write: `TENANT_ADMIN`, `PLATFORM_ADMIN`
- Read: `TENANT_ADMIN`, `SUPPORT_AGENT`, `PLATFORM_ADMIN`

### Create Customer

```http
POST /api/v1/customers
X-Tenant-Slug: acme
Content-Type: application/json
Authorization: Bearer <token>
```

```json
{
  "externalId": "cus_1001",
  "email": "jane@example.com",
  "fullName": "Jane Doe",
  "plan": "PRO"
}
```

### List Customers

```http
GET /api/v1/customers?page=0&size=20
X-Tenant-Slug: acme
Authorization: Bearer <token>
```

### Get Customer By Id

```http
GET /api/v1/customers/{id}
X-Tenant-Slug: acme
Authorization: Bearer <token>
```

## Feedback

Required roles:

- Write: `TENANT_ADMIN`, `PLATFORM_ADMIN`
- Read: `TENANT_ADMIN`, `SUPPORT_AGENT`, `PLATFORM_ADMIN`

### Create Feedback

```http
POST /api/v1/feedbacks
X-Tenant-Slug: acme
Content-Type: application/json
Authorization: Bearer <token>
```

```json
{
  "customerId": "00000000-0000-0000-0000-000000000000",
  "source": "MANUAL",
  "title": "Checkout issue",
  "content": "Payment failed during checkout.",
  "priority": "HIGH",
  "metadata": {
    "channel": "support"
  }
}
```

Supported `source` values:

```text
MANUAL, API, EMAIL, APP_REVIEW
```

Supported `priority` values:

```text
LOW, MEDIUM, HIGH, CRITICAL
```

### List Feedback

```http
GET /api/v1/feedbacks?page=0&size=20
X-Tenant-Slug: acme
Authorization: Bearer <token>
```

Optional filters:

```http
GET /api/v1/feedbacks?status=NEW&priority=HIGH&page=0&size=20
```

Supported `status` values:

```text
NEW, IN_REVIEW, RESOLVED, ARCHIVED
```

### Get Feedback By Id

```http
GET /api/v1/feedbacks/{id}
X-Tenant-Slug: acme
Authorization: Bearer <token>
```

### Feedback Lifecycle

```http
PATCH /api/v1/feedbacks/{id}/status
PATCH /api/v1/feedbacks/{id}/priority
PATCH /api/v1/feedbacks/{id}/assignment
POST  /api/v1/feedbacks/{id}/archive
POST  /api/v1/feedbacks/{id}/restore
POST  /api/v1/feedbacks/{id}/notes
GET   /api/v1/feedbacks/{id}/notes
```

### Search Feedback

```http
GET /api/v1/feedbacks/search?q=checkout&status=NEW&priority=HIGH&page=0&size=20
X-Tenant-Slug: acme
Authorization: Bearer <token>
```

## Automation Rules

Required roles:

- Write: `TENANT_ADMIN`, `PLATFORM_ADMIN`
- Read: `TENANT_ADMIN`, `SUPPORT_AGENT`, `PLATFORM_ADMIN`

### Create Rule

```http
POST /api/v1/automation/rules
X-Tenant-Slug: acme
Content-Type: application/json
Authorization: Bearer <token>
```

```json
{
  "name": "High-risk feedback webhook",
  "description": "Notify an external system when high-risk feedback is enriched.",
  "triggerEventType": "feedback.ai-analysis-completed",
  "conditionJson": {
    "riskLevel": "HIGH"
  },
  "actionJson": [
    {
      "type": "LOG",
      "message": "High-risk feedback detected"
    }
  ],
  "priority": 100
}
```

Current condition support is simple path equality. A condition object matches when every key matches the payload value. Expected values can also be arrays, which match when any array item equals the actual payload value.

Current action types:

```text
LOG, WEBHOOK
```

### List Rules

```http
GET /api/v1/automation/rules?page=0&size=20
X-Tenant-Slug: acme
Authorization: Bearer <token>
```

### Get Rule

```http
GET /api/v1/automation/rules/{ruleId}
X-Tenant-Slug: acme
Authorization: Bearer <token>
```

### Update Rule

```http
PATCH /api/v1/automation/rules/{ruleId}
X-Tenant-Slug: acme
Content-Type: application/json
Authorization: Bearer <token>
```

Every field is optional. Non-null fields update the existing rule.

```json
{
  "name": "Updated rule name",
  "priority": 50
}
```

### Activate Rule

```http
POST /api/v1/automation/rules/{ruleId}/activate
X-Tenant-Slug: acme
Authorization: Bearer <token>
```

### Deactivate Rule

```http
POST /api/v1/automation/rules/{ruleId}/deactivate
X-Tenant-Slug: acme
Authorization: Bearer <token>
```

### Dry Run And Replay

```http
POST /api/v1/automation/rules/{ruleId}/dry-run
POST /api/v1/automation/rules/{ruleId}/replay
X-Tenant-Slug: acme
Authorization: Bearer <token>
```

Automation v2 condition groups support `all` and `any` with operators `eq`, `neq`, `in`, `notIn`, `contains`, `exists`, `gt`, `gte`, `lt`, and `lte`.

## Knowledge And Assistant

Required roles:

- Write knowledge documents: `TENANT_ADMIN`, `PLATFORM_ADMIN`
- Read documents / ask assistant: `TENANT_ADMIN`, `SUPPORT_AGENT`, `PLATFORM_ADMIN`

```http
POST   /api/v1/knowledge/documents
GET    /api/v1/knowledge/documents?page=0&size=20
DELETE /api/v1/knowledge/documents/{documentId}
POST   /api/v1/assistant/questions
X-Tenant-Slug: acme
Authorization: Bearer <token>
```

## Automation Executions

Required roles: `TENANT_ADMIN`, `SUPPORT_AGENT`, `PLATFORM_ADMIN`.

### List Executions

```http
GET /api/v1/automation/executions?page=0&size=20
X-Tenant-Slug: acme
Authorization: Bearer <token>
```

### Get Execution

```http
GET /api/v1/automation/executions/{executionId}
X-Tenant-Slug: acme
Authorization: Bearer <token>
```

### List Action Executions

```http
GET /api/v1/automation/executions/{executionId}/actions
X-Tenant-Slug: acme
Authorization: Bearer <token>
```

## Error Format

Errors use Spring `ProblemDetail` with common custom properties:

```json
{
  "type": "https://insightflow.dev/problems/validation-error",
  "title": "Validation failed",
  "status": 400,
  "detail": "Request validation failed",
  "instance": "/api/v1/feedbacks",
  "errorCode": "VALIDATION_ERROR",
  "timestamp": "2026-07-23T12:00:00Z",
  "correlationId": "demo-correlation-id"
}
```
