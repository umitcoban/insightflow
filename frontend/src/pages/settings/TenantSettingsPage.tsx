import { FormEvent } from "react";
import { Save, Settings } from "lucide-react";
import { Badge } from "../../components/ui/badge";
import { Button } from "../../components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "../../components/ui/card";
import { Field, Textarea } from "../../components/ui/input";
import { PageHeader } from "../../components/ui/page-header";
import { useToast } from "../../components/ui/toast";
import { useAuth } from "../../auth/AuthProvider";
import { parseJsonObject, prettyJson } from "../../lib/json";
import { toMessage } from "../../lib/errors";
import { useTenant, useTenantMutations, useTenantSettings } from "./hooks";

export function TenantSettingsPage() {
  const { session } = useAuth();
  const tenant = useTenant();
  const settings = useTenantSettings();
  const mutations = useTenantMutations();
  const { notify } = useToast();

  async function updateSettings(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    try {
      const payload = parseJsonObject(new FormData(event.currentTarget).get("settings")?.toString() ?? "{}", "Tenant settings");
      await mutations.updateSettings.mutateAsync(payload);
      notify("Tenant settings updated");
    } catch (error) {
      notify("Tenant settings failed", toMessage(error));
    }
  }

  return (
    <>
      <PageHeader eyebrow="Settings" title="Tenant settings" description="Manage tenant lifecycle and generic settings JSON." actions={tenant.data ? <Badge>{tenant.data.status}</Badge> : null} />
      <div className="grid gap-5 lg:grid-cols-[1fr_360px]">
        <Card>
          <CardHeader><CardTitle className="flex items-center gap-2"><Settings size={18} /> Settings JSON</CardTitle></CardHeader>
          <CardContent>
            <form className="grid gap-4" onSubmit={updateSettings}>
              <Field label="Settings">
                <Textarea name="settings" className="min-h-96 font-mono text-xs" defaultValue={prettyJson(settings.data?.settings ?? {})} />
              </Field>
              <Button><Save size={16} /> Save settings</Button>
            </form>
          </CardContent>
        </Card>
        <Card>
          <CardHeader><CardTitle>{session?.tenantSlug}</CardTitle></CardHeader>
          <CardContent className="grid gap-3 text-sm">
            <div className="rounded-lg bg-slate-50 p-3"><strong>Name</strong><p className="text-slate-600">{tenant.data?.name ?? "Loading"}</p></div>
            <div className="rounded-lg bg-slate-50 p-3"><strong>Status</strong><p className="text-slate-600">{tenant.data?.status ?? "UNKNOWN"}</p></div>
            {tenant.data?.status === "SUSPENDED"
              ? <Button onClick={() => mutations.reactivate.mutate()}>Reactivate tenant</Button>
              : <Button variant="danger" onClick={() => mutations.suspend.mutate()}>Suspend tenant</Button>}
          </CardContent>
        </Card>
      </div>
    </>
  );
}
