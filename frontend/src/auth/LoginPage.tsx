import { FormEvent, useState } from "react";
import { ArrowRight } from "lucide-react";
import { Navigate, useLocation, useNavigate } from "react-router";
import { Button } from "../components/ui/button";
import { Card, CardContent } from "../components/ui/card";
import { Field, Input, Select } from "../components/ui/input";
import { useToast } from "../components/ui/toast";
import { toMessage } from "../lib/errors";
import { useAuth } from "./AuthProvider";
import { demoUsers, loginWithPassword } from "./auth.api";

export function LoginPage() {
  const { isAuthenticated, setSession } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const { notify } = useToast();
  const [selected, setSelected] = useState(demoUsers[0]);
  const [tenantSlug, setTenantSlug] = useState("acme");
  const [busy, setBusy] = useState(false);

  if (isAuthenticated) {
    return <Navigate to="/app/dashboard" replace />;
  }

  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setBusy(true);
    try {
      const nextSession = await loginWithPassword(selected.username, selected.password, selected.role, tenantSlug);
      setSession(nextSession);
      navigate((location.state as { from?: string } | null)?.from ?? "/app/dashboard", { replace: true });
    } catch (error) {
      notify("Login failed", toMessage(error));
    } finally {
      setBusy(false);
    }
  }

  return (
    <main className="grid min-h-screen place-items-center bg-slate-50 p-5">
      <div className="grid w-full max-w-5xl gap-6 lg:grid-cols-[1fr_420px]">
        <section className="grid content-center rounded-lg border border-slate-200 bg-white p-8">
          <p className="text-xs font-bold uppercase tracking-wide text-teal-700">Secure tenant workspace</p>
          <h1 className="mt-3 text-4xl font-semibold tracking-tight text-slate-950">Sign in to operate customer intelligence.</h1>
          <p className="mt-4 max-w-2xl text-sm leading-6 text-slate-600">
            Demo authentication uses the local Keycloak realm. The app will attach the JWT and tenant slug to every protected API request.
          </p>
        </section>
        <Card>
          <CardContent>
            <form className="grid gap-4" onSubmit={submit}>
              <Field label="Demo identity">
                <Select value={selected.username} onChange={(event) => setSelected(demoUsers.find((user) => user.username === event.target.value) ?? demoUsers[0])}>
                  {demoUsers.map((user) => <option key={user.username} value={user.username}>{user.username} · {user.role}</option>)}
                </Select>
              </Field>
              <Field label="Tenant slug">
                <Input value={tenantSlug} onChange={(event) => setTenantSlug(event.target.value)} />
              </Field>
              <Button disabled={busy}>{busy ? "Connecting..." : "Enter console"} <ArrowRight size={16} /></Button>
            </form>
          </CardContent>
        </Card>
      </div>
    </main>
  );
}
