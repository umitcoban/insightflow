# InsightFlow Demo Runbook

This runbook demonstrates the current MVP backend flow.

## Prerequisites

- JDK 25 available at `C:\Users\implo\.jdks\openjdk-25.0.1` on this machine.
- Docker Desktop running.
- Local infrastructure started through Docker Compose.

## Start Infrastructure

```powershell
docker compose up -d
```

Pull the local Ollama models before calling AI-backed endpoints:

```bash
docker compose exec ollama ollama pull llama3.1
docker compose exec ollama ollama pull mxbai-embed-large
```

Services:

- PostgreSQL: `localhost:5432`
- Kafka: `localhost:9092`
- Keycloak: `http://localhost:8081`

Keycloak imports `docker/keycloak/insightflow-realm.json` on startup.

## Run The API

```powershell
$env:JAVA_HOME='C:\Users\implo\.jdks\openjdk-25.0.1'
$env:PATH="$env:JAVA_HOME\bin;$env:PATH"
.\gradlew.bat bootRun --args='--spring.profiles.active=local'
```

The API runs on:

```text
http://localhost:8080
```

## Get Development Tokens

Use `http/auth.http` from IntelliJ HTTP Client.

Development users:

- `platform-admin` / `platform-admin`
- `acme-admin` / `acme-admin`
- `acme-agent` / `acme-agent`

The Acme tenant is seeded with:

```text
tenant_id: 11111111-1111-1111-1111-111111111111
tenant_slug: acme
```

## Demo Flow

1. Use the `acme-admin` token.
2. Create a customer with `POST /api/v1/customers`.
3. Create feedback for that customer with `POST /api/v1/feedbacks`.
4. Wait for the outbox publisher and Kafka consumer loop.
5. List feedback with `GET /api/v1/feedbacks`.
6. Confirm AI fields are populated by the mock analyzer:
   - `sentiment`
   - `category`
   - `riskLevel`
   - `aiSummary`
   - `suggestedAction`
7. Create an automation rule for `feedback.ai-analysis-completed`.
8. Create another feedback item that matches the rule condition.
9. List automation executions with `GET /api/v1/automation/executions`.
10. Inspect action results with `GET /api/v1/automation/executions/{executionId}/actions`.

## Health Check

```http
GET http://localhost:8080/actuator/health
```

## Verification

Run tests with the explicit JDK path:

```powershell
$env:JAVA_HOME='C:\Users\implo\.jdks\openjdk-25.0.1'
$env:PATH="$env:JAVA_HOME\bin;$env:PATH"
.\gradlew.bat test
```
