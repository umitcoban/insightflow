import { ApiError } from "../lib/errors";
import { Session, UserRole } from "./session";

const keycloakBaseUrl = import.meta.env.VITE_KEYCLOAK_BASE_URL ?? "";

export const demoUsers = [
  { label: "Tenant admin", username: "acme-admin", password: "acme-admin" },
  { label: "Support agent", username: "acme-agent", password: "acme-agent" }
];

export async function loginWithPassword(username: string, password: string, tenantSlug: string): Promise<Session> {
  const response = await fetch(`${keycloakBaseUrl}/realms/insightflow/protocol/openid-connect/token`, {
    method: "POST",
    headers: { "Content-Type": "application/x-www-form-urlencoded" },
    body: new URLSearchParams({
      grant_type: "password",
      client_id: "insightflow-dev-client",
      username,
      password
    })
  });

  if (!response.ok) {
    throw new ApiError("Login failed", response.status, await safeJson(response));
  }

  const payload = (await response.json()) as { access_token: string };
  return { accessToken: payload.access_token, username, role: resolveRole(payload.access_token, username), tenantSlug };
}

async function safeJson(response: Response) {
  try {
    return await response.json();
  } catch {
    return undefined;
  }
}

function resolveRole(accessToken: string, username: string): UserRole {
  const roles = readJwtRoles(accessToken);
  if (roles.includes("PLATFORM_ADMIN")) return "PLATFORM_ADMIN";
  if (roles.includes("TENANT_ADMIN")) return "TENANT_ADMIN";
  if (roles.includes("SUPPORT_AGENT")) return "SUPPORT_AGENT";
  if (username.includes("admin")) return "TENANT_ADMIN";
  return "SUPPORT_AGENT";
}

function readJwtRoles(accessToken: string) {
  try {
    const [, encodedPayload] = accessToken.split(".");
    const normalized = encodedPayload.replace(/-/g, "+").replace(/_/g, "/");
    const padded = normalized.padEnd(normalized.length + (4 - normalized.length % 4) % 4, "=");
    const decoded = JSON.parse(atob(padded)) as {
      realm_access?: { roles?: string[] };
      resource_access?: Record<string, { roles?: string[] }>;
    };
    return [
      ...(decoded.realm_access?.roles ?? []),
      ...Object.values(decoded.resource_access ?? {}).flatMap((resource) => resource.roles ?? [])
    ];
  } catch {
    return [];
  }
}
