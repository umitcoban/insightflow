import { Activity, AlertTriangle } from "lucide-react";
import { Card, CardContent, CardHeader, CardTitle } from "../../components/ui/card";
import { EmptyState } from "../../components/ui/empty-state";
import { PageHeader } from "../../components/ui/page-header";
import { StatusDot } from "../../components/data/status-dot";
import { useHealth, useReadiness } from "./hooks";

export function OperationsPage() {
  const health = useHealth();
  const readiness = useReadiness();
  const components = health.data?.components ?? {};

  return (
    <>
      <PageHeader eyebrow="Operations" title="Runtime readiness" description="Track API, dependency and model readiness from actuator health signals." />
      <div className="grid gap-4 md:grid-cols-2">
        <Card><CardContent><Status label="Health" value={health.data?.status} /></CardContent></Card>
        <Card><CardContent><Status label="Readiness" value={readiness.data?.status} /></CardContent></Card>
      </div>
      <Card>
        <CardHeader><CardTitle>Health Components</CardTitle></CardHeader>
        <CardContent>
          {Object.keys(components).length ? (
            <div className="grid gap-3 md:grid-cols-2 xl:grid-cols-3">
              {Object.entries(components).map(([name, component]) => (
                <div key={name} className="rounded-lg border border-slate-200 p-4">
                  <Status label={name} value={component.status} />
                  {component.details ? <pre className="mt-3 max-h-32 overflow-auto rounded bg-slate-50 p-2 text-xs text-slate-600">{JSON.stringify(component.details, null, 2)}</pre> : null}
                </div>
              ))}
            </div>
          ) : <EmptyState icon={<Activity />} title="No component details" text="Expose actuator health components to inspect dependency readiness." />}
        </CardContent>
      </Card>
      <Card>
        <CardHeader><CardTitle>Troubleshooting Hints</CardTitle></CardHeader>
        <CardContent className="grid gap-3 text-sm text-slate-600">
          {["Ollama chat model missing: pull llama3.2:1b.", "Vector search unavailable: verify Elasticsearch and knowledge index.", "Tenant not found: run local profile seeder or create tenant.", "Unauthorized: sign in again and verify tenant slug."].map((item) => (
            <div key={item} className="flex gap-3 rounded-lg bg-amber-50 p-3 text-amber-900"><AlertTriangle size={16} /> {item}</div>
          ))}
        </CardContent>
      </Card>
    </>
  );
}

function Status({ label, value }: { label: string; value?: string }) {
  return <div className="flex items-center justify-between gap-3"><span className="flex items-center gap-3 font-semibold"><StatusDot status={value} /> {label}</span><span className="text-sm text-slate-500">{value ?? "UNKNOWN"}</span></div>;
}
