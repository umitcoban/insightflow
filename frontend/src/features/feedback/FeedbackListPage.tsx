import { FormEvent, useState } from "react";
import { Link, useNavigate } from "react-router";
import { MessageSquareText, Plus, Search } from "lucide-react";
import { Badge } from "../../components/ui/badge";
import { Button } from "../../components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "../../components/ui/card";
import { EmptyState } from "../../components/ui/empty-state";
import { Field, Input, Select, Textarea } from "../../components/ui/input";
import { PageHeader } from "../../components/ui/page-header";
import { useToast } from "../../components/ui/toast";
import { useCustomers } from "../customers/hooks";
import { parseJsonObject } from "../../lib/json";
import { toMessage } from "../../lib/errors";
import { useFeedbackMutations, useFeedbacks } from "./hooks";
import { FeedbackFilters } from "./api";

const sources = ["", "MANUAL", "API", "EMAIL", "APP_REVIEW"];
const priorities = ["", "LOW", "MEDIUM", "HIGH", "CRITICAL"];
const statuses = ["", "NEW", "IN_REVIEW", "RESOLVED", "ARCHIVED"];
const sentiments = ["", "POSITIVE", "NEUTRAL", "NEGATIVE"];
const risks = ["", "LOW", "MEDIUM", "HIGH", "CHURN_RISK"];

export function FeedbackListPage() {
  const navigate = useNavigate();
  const { notify } = useToast();
  const [filters, setFilters] = useState<FeedbackFilters>({});
  const feedbacks = useFeedbacks(filters);
  const customers = useCustomers();
  const mutations = useFeedbackMutations();

  async function createFeedback(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const formElement = event.currentTarget;
    const form = new FormData(formElement);
    try {
      const feedback = await mutations.create.mutateAsync({
        customerId: form.get("customerId")?.toString() || undefined,
        source: form.get("source")?.toString(),
        priority: form.get("priority")?.toString(),
        title: form.get("title")?.toString(),
        content: form.get("content")?.toString(),
        metadata: parseJsonObject(form.get("metadata")?.toString() ?? "{}", "Metadata")
      });
      formElement.reset();
      notify("Feedback created", "AI enrichment will update the detail page when processed.");
      navigate(`/app/feedback/${feedback.id}`);
    } catch (error) {
      notify("Feedback create failed", toMessage(error));
    }
  }

  return (
    <>
      <PageHeader eyebrow="Feedback" title="Feedback queue" description="Search, classify and follow customer signals through the full lifecycle." />
      <Card>
        <CardContent>
          <div className="grid gap-3 lg:grid-cols-8">
            <Field label="Search"><Input value={filters.q ?? ""} onChange={(event) => setFilters({ ...filters, q: event.target.value })} placeholder="payment, refund, churn..." /></Field>
            <Field label="Status"><Select value={filters.status ?? ""} onChange={(event) => setFilters({ ...filters, status: event.target.value || undefined })}>{statuses.map((x) => <option key={x} value={x}>{x || "Any"}</option>)}</Select></Field>
            <Field label="Priority"><Select value={filters.priority ?? ""} onChange={(event) => setFilters({ ...filters, priority: event.target.value || undefined })}>{priorities.map((x) => <option key={x} value={x}>{x || "Any"}</option>)}</Select></Field>
            <Field label="Sentiment"><Select value={filters.sentiment ?? ""} onChange={(event) => setFilters({ ...filters, sentiment: event.target.value || undefined })}>{sentiments.map((x) => <option key={x} value={x}>{x || "Any"}</option>)}</Select></Field>
            <Field label="Risk"><Select value={filters.riskLevel ?? ""} onChange={(event) => setFilters({ ...filters, riskLevel: event.target.value || undefined })}>{risks.map((x) => <option key={x} value={x}>{x || "Any"}</option>)}</Select></Field>
            <Field label="Source"><Select value={filters.source ?? ""} onChange={(event) => setFilters({ ...filters, source: event.target.value || undefined })}>{sources.map((x) => <option key={x} value={x}>{x || "Any"}</option>)}</Select></Field>
            <Field label="From"><Input type="date" value={filters.from ?? ""} onChange={(event) => setFilters({ ...filters, from: event.target.value || undefined })} /></Field>
            <Field label="To"><Input type="date" value={filters.to ?? ""} onChange={(event) => setFilters({ ...filters, to: event.target.value || undefined })} /></Field>
          </div>
        </CardContent>
      </Card>
      <div className="grid gap-5 xl:grid-cols-[1fr_420px]">
        <Card>
          <CardHeader><CardTitle className="flex items-center gap-2"><Search size={18} /> Results</CardTitle><span className="text-sm text-slate-500">{feedbacks.data?.totalElements ?? 0} records</span></CardHeader>
          <CardContent className="grid gap-3">
            {feedbacks.data?.content.length ? feedbacks.data.content.map((item) => (
              <Link key={item.id} to={`/app/feedback/${item.id}`} className="rounded-lg border border-slate-200 p-4 hover:bg-slate-50">
                <div className="flex flex-wrap items-center justify-between gap-2">
                  <strong>{item.title}</strong>
                  <div className="flex gap-2"><Badge>{item.priority}</Badge><Badge>{item.status}</Badge>{item.sentiment ? <Badge>{item.sentiment}</Badge> : null}</div>
                </div>
                <p className="mt-2 line-clamp-2 text-sm leading-6 text-slate-600">{item.aiSummary ?? item.content}</p>
              </Link>
            )) : <EmptyState icon={<MessageSquareText />} title="No feedback" text="Create feedback or adjust filters." />}
          </CardContent>
        </Card>
        <Card>
          <CardHeader><CardTitle>Create Feedback</CardTitle></CardHeader>
          <CardContent>
            <form className="grid gap-4" onSubmit={createFeedback}>
              <Field label="Customer"><Select name="customerId" defaultValue=""><option value="">No customer</option>{(customers.data?.content ?? []).map((customer) => <option key={customer.id} value={customer.id}>{customer.fullName}</option>)}</Select></Field>
              <div className="grid gap-3 md:grid-cols-2">
                <Field label="Source"><Select name="source" defaultValue="MANUAL">{sources.filter(Boolean).map((x) => <option key={x} value={x}>{x}</option>)}</Select></Field>
                <Field label="Priority"><Select name="priority" defaultValue="MEDIUM">{priorities.filter(Boolean).map((x) => <option key={x} value={x}>{x}</option>)}</Select></Field>
              </div>
              <Field label="Title"><Input name="title" required maxLength={200} /></Field>
              <Field label="Content"><Textarea name="content" required /></Field>
              <Field label="Metadata JSON"><Textarea name="metadata" defaultValue={'{"channel":"web"}'} className="font-mono text-xs" /></Field>
              <Button disabled={mutations.create.isPending}><Plus size={16} /> Create feedback</Button>
            </form>
          </CardContent>
        </Card>
      </div>
    </>
  );
}
