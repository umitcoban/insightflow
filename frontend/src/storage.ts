import type { Session } from "./api";

const SESSION_KEY = "insightflow.session";

export function loadSession(): Session | null {
  const value = localStorage.getItem(SESSION_KEY);
  if (!value) {
    return null;
  }
  try {
    return JSON.parse(value) as Session;
  } catch {
    localStorage.removeItem(SESSION_KEY);
    return null;
  }
}

export function saveSession(session: Session): void {
  localStorage.setItem(SESSION_KEY, JSON.stringify(session));
}

export function clearSession(): void {
  localStorage.removeItem(SESSION_KEY);
}
