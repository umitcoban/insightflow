import { AlertCircle, Bot, FileText, MessageSquareText, Users, Zap } from "lucide-react";
import { Link } from "react-router";
import { Badge } from "../../components/ui/badge";
import { Card, CardContent, CardHeader, CardTitle } from "../../components/ui/card";
import { EmptyState } from "../../components/ui/empty-state";
import { PageHeader } from "../../components/ui/page-header";
import { StatusDot } from "../../components/data/status-dot";
import { useAutomationExecutions, useAutomationRules } from "../../features/automation/hooks";
import { useCustomers } from "../../features/customers/hooks";
import { useFeedbacks } from "../../features/feedback/hooks";
import { useKnowledgeDocuments } from "../../features/knowledge/hooks";
import { useHealth, useReadiness } from "../operations/hooks";

export function DashboardPage() {
  const feedbacks = useFeedbacks({});
  const customers = useCustomers();
  const rules = useAutomationRules();
  const executions = useAutomationExecutions();
  const documents = useKnowledgeDocuments();
  const health = useHealth();
  const readiness = useReadiness();
  const feedbackItems = feedbacks.data?.content ?? [];
  const open = feedbackItems.filter((item) => item.status !== "RESOLVED" && item.status !== "ARCHIVED").length;
  const critical = feedbackItems.filter((item) => item.priority === "CRITICAL").length;
  const enriched = feedbackItems.filter((item) => item.aiSummary || item.sentiment).length;

  return (
    <>
      <PageHeader eyebrow="Dashboard" title="Tenant operations at a glance" description="Live feedback, automation, knowledge and readiness signals in one workspace." />
      <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-5">
        <Metric title="Open feedback" value={open} icon={<MessageSquareText size={20} />} />
        <Metric title="Critical" value={critical} icon={<AlertCircle size={20} />} />
        <Metric title="AI enriched" value={enriched} icon={<Bot size={20} />} />
        <Metric title="Customers" value={customers.data?.content.length ?? 0} icon={<Users size={20} />} />
        <Metric title="Rules" value={rules.data?.content.length ?? 0} icon={<Zap size={20} />} />
      </div>
      <div className="grid gap-4 lg:grid-cols-[1.15fr_0.85fr]">
        <Card>
          <CardHeader><CardTitle>Recent Feedback</CardTitle><Link className="text-sm font-semibold text-teal-700" to="/app/feedback">View all</Link></CardHeader>
          <CardContent className="grid gap-3">
            {feedbackItems.length ? feedbackItems.slice(0, 6).map((item) => (
              <Link key={item.id} to={`/app/feedback/${item.id}`} className="rounded-lg border border-slate-200 p-3 hover:bg-slate-50">
                <div className="flex items-center justify-between gap-3">
                  <strong className="text-sm">{item.title}</strong>
                  <Badge>{item.priority}</Badge>
                </div>
                <p className="mt-2 line-clamp-2 text-sm text-slate-600">{item.aiSummary ?? item.content}</p>
              </Link>
            )) : <EmptyState icon={<MessageSquareText />} title="No feedback" text="Create feedback to start the operational loop." />}
          </CardContent>
        </Card>
        <div className="grid gap-4">
          <Card>
            <CardHeader><CardTitle>Readiness</CardTitle></CardHeader>
            <CardContent className="grid gap-3">
              <HealthRow label="API health" status={health.data?.status} />
              <HealthRow label="Readiness" status={readiness.data?.status} />
              <HealthRow label="Knowledge docs" status={documents.data?.content.length ? "UP" : "UNKNOWN"} extra={`${documents.data?.content.length ?? 0} docs`} />
            </CardContent>
          </Card>
          <Card>
            <CardHeader><CardTitle>Automation Pulse</CardTitle></CardHeader>
            <CardContent className="grid gap-3">
              {(executions.data?.content ?? []).slice(0, 5).map((item) => (
                <div key={item.id} className="flex items-center justify-between rounded-lg border border-slate-200 p-3 text-sm">
                  <span>{item.sourceEventType}</span>
                  <Badge>{item.status}</Badge>
                </div>
              ))}
            </CardContent>
          </Card>
        </div>
      </div>
    </>
  );
}

function Metric({ title, value, icon }: { title: string; value: number; icon: React.ReactNode }) {
  return (
    <Card>
      <CardContent>
        <div className="mb-4 grid h-10 w-10 place-items-center rounded-lg bg-teal-50 text-teal-800">{icon}</div>
        <span className="text-sm text-slate-500">{title}</span>
        <strong className="mt-1 block text-3xl">{value}</strong>
      </CardContent>
    </Card>
  );
}

function HealthRow({ label, status, extra }: { label: string; status?: string; extra?: string }) {
  return (
    <div className="flex items-center justify-between rounded-lg border border-slate-200 p-3">
      <span className="flex items-center gap-3 text-sm"><StatusDot status={status} /> {label}</span>
      <span className="text-sm text-slate-500">{extra ?? status ?? "UNKNOWN"}</span>
    </div>
  );
}
