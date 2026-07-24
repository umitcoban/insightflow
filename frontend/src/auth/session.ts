export type UserRole = "TENANT_ADMIN" | "SUPPORT_AGENT" | "PLATFORM_ADMIN";

export type Session = {
  accessToken: string;
  username: string;
  role: UserRole;
  tenantSlug: string;
};

const storageKey = "insightflow.session";

export function loadSession(): Session | null {
  const raw = localStorage.getItem(storageKey);
  if (!raw) {
    return null;
  }
  try {
    return JSON.parse(raw) as Session;
  } catch {
    localStorage.removeItem(storageKey);
    return null;
  }
}

export function saveSession(session: Session) {
  localStorage.setItem(storageKey, JSON.stringify(session));
}

export function clearSession() {
  localStorage.removeItem(storageKey);
}
