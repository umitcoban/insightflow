import { ApiError } from "../lib/errors";
import { Session, UserRole } from "./session";

const keycloakBaseUrl = import.meta.env.VITE_KEYCLOAK_BASE_URL ?? "";

export const demoUsers = [
  { username: "acme-admin", password: "acme-admin", role: "TENANT_ADMIN" as UserRole },
  { username: "acme-agent", password: "acme-agent", role: "SUPPORT_AGENT" as UserRole },
  { username: "platform-admin", password: "platform-admin", role: "PLATFORM_ADMIN" as UserRole }
];

export async function loginWithPassword(username: string, password: string, role: UserRole, tenantSlug: string): Promise<Session> {
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
  return { accessToken: payload.access_token, username, role, tenantSlug };
}

async function safeJson(response: Response) {
  try {
    return await response.json();
  } catch {
    return undefined;
  }
}
