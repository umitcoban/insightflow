import { createContext, ReactNode, useContext, useEffect, useMemo, useState } from "react";
import { clearSession, loadSession, saveSession, Session } from "./session";

type AuthContextValue = {
  session: Session | null;
  isAuthenticated: boolean;
  setSession: (session: Session) => void;
  signOut: () => void;
};

const AuthContext = createContext<AuthContextValue | null>(null);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [session, setStoredSession] = useState<Session | null>(() => loadSession());

  useEffect(() => {
    function handleUnauthorized() {
      clearSession();
      setStoredSession(null);
    }
    window.addEventListener("insightflow:unauthorized", handleUnauthorized);
    return () => window.removeEventListener("insightflow:unauthorized", handleUnauthorized);
  }, []);

  const value = useMemo<AuthContextValue>(() => ({
    session,
    isAuthenticated: Boolean(session?.accessToken),
    setSession(nextSession) {
      saveSession(nextSession);
      setStoredSession(nextSession);
    },
    signOut() {
      clearSession();
      setStoredSession(null);
    }
  }), [session]);

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const value = useContext(AuthContext);
  if (!value) {
    throw new Error("useAuth must be used inside AuthProvider");
  }
  return value;
}
