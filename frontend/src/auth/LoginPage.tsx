import { FormEvent, useState } from "react";
import { ArrowRight, LockKeyhole, UserRound } from "lucide-react";
import { Navigate, useLocation, useNavigate } from "react-router";
import { Button } from "../components/ui/button";
import { Card, CardContent } from "../components/ui/card";
import { Field, Input } from "../components/ui/input";
import { useToast } from "../components/ui/toast";
import { toMessage } from "../lib/errors";
import { useAuth } from "./AuthProvider";
import { demoUsers, loginWithPassword } from "./auth.api";

export function LoginPage() {
  const { isAuthenticated, setSession } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const { notify } = useToast();
  const [username, setUsername] = useState(demoUsers[0].username);
  const [password, setPassword] = useState(demoUsers[0].password);
  const [tenantSlug, setTenantSlug] = useState("acme");
  const [busy, setBusy] = useState(false);

  if (isAuthenticated) {
    return <Navigate to="/app/dashboard" replace />;
  }

  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setBusy(true);
    try {
      const nextSession = await loginWithPassword(username, password, tenantSlug);
      setSession(nextSession);
      navigate((location.state as { from?: string } | null)?.from ?? "/app/dashboard", { replace: true });
    } catch (error) {
      notify("Login failed", toMessage(error));
    } finally {
      setBusy(false);
    }
  }

  return (
    <main className="grid min-h-screen place-items-center bg-[#f5f8f7] p-5">
      <div className="grid w-full max-w-6xl gap-6 lg:grid-cols-[1fr_430px]">
        <section className="grid content-center rounded-lg border border-slate-200 bg-white p-8 shadow-sm">
          <div className="grid h-12 w-12 place-items-center rounded-lg bg-teal-700 text-sm font-bold text-white">IF</div>
          <p className="mt-6 text-xs font-bold uppercase tracking-wide text-teal-700">InsightFlow workspace</p>
          <h1 className="mt-3 max-w-2xl text-4xl font-semibold tracking-tight text-slate-950">Sign in to manage tenant operations.</h1>
          <p className="mt-4 max-w-2xl text-sm leading-6 text-slate-600">
            Use your workspace credentials. InsightFlow will derive permissions from the access token and scope every request to the selected tenant.
          </p>
          <div className="mt-7 grid gap-3 sm:grid-cols-2">
            {["Feedback triage", "Knowledge assistant", "Automation workflows", "Operations health"].map((item) => (
              <div key={item} className="rounded-lg border border-slate-200 bg-slate-50 p-3 text-sm font-semibold text-slate-700">{item}</div>
            ))}
          </div>
        </section>
        <Card>
          <CardContent>
            <form className="grid gap-4" onSubmit={submit}>
              <div>
                <h2 className="text-xl font-semibold text-slate-950">Welcome back</h2>
                <p className="mt-1 text-sm text-slate-500">Choose a tenant and sign in with a real workspace identity.</p>
              </div>
              <Field label="Username">
                <Input value={username} onChange={(event) => setUsername(event.target.value)} autoComplete="username" />
              </Field>
              <Field label="Password">
                <Input value={password} onChange={(event) => setPassword(event.target.value)} type="password" autoComplete="current-password" />
              </Field>
              <Field label="Tenant slug">
                <Input value={tenantSlug} onChange={(event) => setTenantSlug(event.target.value)} />
              </Field>
              <Button disabled={busy}>{busy ? "Connecting..." : "Enter console"} <ArrowRight size={16} /></Button>
              <div className="grid gap-2 border-t border-slate-100 pt-4">
                <span className="text-xs font-bold uppercase text-slate-500">Demo shortcuts</span>
                <div className="grid gap-2 sm:grid-cols-2">
                  {demoUsers.map((user) => (
                    <Button
                      key={user.username}
                      type="button"
                      variant="secondary"
                      onClick={() => {
                        setUsername(user.username);
                        setPassword(user.password);
                      }}
                    >
                      {user.label === "Tenant admin" ? <LockKeyhole size={16} /> : <UserRound size={16} />}
                      {user.label}
                    </Button>
                  ))}
                </div>
              </div>
            </form>
          </CardContent>
        </Card>
      </div>
    </main>
  );
}
