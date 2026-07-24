# InsightFlow Production Runbook

## Target

The first production deployment target is a single-server Docker Compose environment.

## Required Secrets

Create `.env` from `.env.example` and replace every placeholder:

```bash
cp .env.example .env
```

Required values:

- `POSTGRES_PASSWORD`
- `INSIGHTFLOW_SECURITY_JWT_ISSUER_URI`
- `INSIGHTFLOW_SECURITY_JWT_AUDIENCE`

The default AI backend is Spring AI with Ollama. `OPENAI_API_KEY` is optional and only needed when `SPRING_AI_MODEL_CHAT=openai` or `SPRING_AI_MODEL_EMBEDDING=openai`.

## Start

```bash
docker compose --env-file .env -f docker-compose.prod.yml up -d --build
```

Pull the default local models before serving AI-backed endpoints:

```bash
docker compose --env-file .env -f docker-compose.prod.yml exec ollama ollama pull llama3.1
docker compose --env-file .env -f docker-compose.prod.yml exec ollama ollama pull mxbai-embed-large
```

## Verify

```bash
curl http://localhost:8080/actuator/health
curl http://localhost:8080/actuator/health/readiness
curl http://localhost:8080/actuator/prometheus
```

## Keycloak

Production must use an external production-mode Keycloak deployment with persistent database storage, real TLS/proxy configuration, and the same issuer/audience used by the API.
