# InsightFlow Frontend

React + Vite based operations console for the InsightFlow backend.

## Local Run

Start backend infrastructure and API first:

```powershell
docker compose up -d
docker compose exec ollama ollama pull llama3.1
docker compose exec ollama ollama pull mxbai-embed-large

$env:JAVA_HOME='C:\Users\implo\.jdks\openjdk-25.0.1'
$env:PATH="$env:JAVA_HOME\bin;$env:PATH"
.\gradlew.bat bootRun --args='--spring.profiles.active=local'
```

Then start the frontend:

```powershell
cd frontend
npm install
npm run dev
```

Default dev URL:

```text
http://localhost:5173
```

The Vite dev server proxies `/api` and `/actuator` to `http://localhost:8080`, and `/realms` to `http://localhost:8081`.

## Demo Users

- `acme-admin / acme-admin`
- `acme-agent / acme-agent`
- `platform-admin / platform-admin`

Use tenant slug `acme` for tenant-scoped screens.
