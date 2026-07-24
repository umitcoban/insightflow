import { FormEvent, useState } from "react";
import { Link, useParams } from "react-router";
import { Archive, Bot, FileText, RefreshCcw, Save, Sparkles } from "lucide-react";
import { Badge } from "../../components/ui/badge";
import { Button } from "../../components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "../../components/ui/card";
import { EmptyState } from "../../components/ui/empty-state";
import { Field, Input, Select } from "../../components/ui/input";
import { PageHeader } from "../../components/ui/page-header";
import { useToast } from "../../components/ui/toast";
import { formatDate } from "../../lib/dates";
import { toMessage } from "../../lib/errors";
import { useCustomer } from "../customers/hooks";
import { useFeedback, useFeedbackMutations, useFeedbackNotes } from "./hooks";

export function FeedbackDetailPage() {
  const { feedbackId } = useParams();
  const { notify } = useToast();
  const feedback = useFeedback(feedbackId);
  const notes = useFeedbackNotes(feedbackId);
  const mutations = useFeedbackMutations(feedbackId);
  const customer = useCustomer(feedback.data?.customerId);
  const [assignedTo, setAssignedTo] = useState("");

  async function run(action: () => Promise<unknown>, title: string) {
    try {
      await action();
      notify(title);
    } catch (error) {
      notify(`${title} failed`, toMessage(error));
    }
  }

  async function addNote(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const formElement = event.currentTarget;
    const content = new FormData(formElement).get("content")?.toString() ?? "";
    await run(() => mutations.note.mutateAsync(content), "Note added");
    formElement.reset();
  }

  const item = feedback.data;
  return (
    <>
      <PageHeader eyebrow="Feedback detail" title={item?.title ?? "Loading feedback"} description="Inspect customer signal, AI enrichment, owner actions and internal notes." actions={<Button variant="secondary" onClick={() => feedback.refetch()}><RefreshCcw size={16} /> Refresh</Button>} />
      {item ? (
        <div className="grid gap-5 xl:grid-cols-[1fr_420px]">
          <div className="grid gap-5">
            <Card>
              <CardHeader><CardTitle>Signal</CardTitle><div className="flex gap-2"><Badge>{item.source}</Badge><Badge>{item.priority}</Badge><Badge>{item.status}</Badge></div></CardHeader>
              <CardContent>
                <p className="whitespace-pre-wrap text-sm leading-7 text-slate-700">{item.content}</p>
              </CardContent>
            </Card>
            <Card>
              <CardHeader><CardTitle className="flex items-center gap-2"><Bot size={18} /> AI Analysis</CardTitle><Button variant="secondary" size="sm" onClick={() => run(() => mutations.analyze.mutateAsync(), "Feedback analyzed")}><Sparkles size={16} /> Analyze</Button></CardHeader>
              <CardContent>
                {item.aiSummary || item.sentiment ? (
                  <div className="grid gap-4">
                    <p className="text-sm leading-7 text-slate-700">{item.aiSummary}</p>
                    <div className="flex flex-wrap gap-2">{item.sentiment ? <Badge>{item.sentiment}</Badge> : null}{item.category ? <Badge>{item.category}</Badge> : null}{item.riskLevel ? <Badge>{item.riskLevel}</Badge> : null}</div>
                    {item.suggestedAction ? <div className="rounded-lg bg-teal-50 p-4 text-sm leading-6 text-teal-950">{item.suggestedAction}</div> : null}
                  </div>
                ) : <EmptyState icon={<Bot />} title="AI pending" text="Run analysis manually or wait for the Kafka enrichment consumer." />}
              </CardContent>
            </Card>
            <Card>
              <CardHeader><CardTitle>Notes</CardTitle></CardHeader>
              <CardContent className="grid gap-4">
                <form className="flex gap-2" onSubmit={addNote}>
                  <Input name="content" required placeholder="Add internal note" />
                  <Button variant="secondary">Add</Button>
                </form>
                {(notes.data ?? []).length ? notes.data!.map((note) => (
                  <div key={note.id} className="rounded-lg border border-slate-200 p-3">
                    <div className="flex justify-between gap-3 text-sm"><strong>{note.author}</strong><span className="text-slate-500">{formatDate(note.createdAt)}</span></div>
                    <p className="mt-2 text-sm text-slate-600">{note.content}</p>
                  </div>
                )) : <EmptyState icon={<FileText />} title="No notes" text="Add support context for this item." />}
              </CardContent>
            </Card>
          </div>
          <div className="grid content-start gap-5">
            <Card>
              <CardHeader><CardTitle>Lifecycle</CardTitle></CardHeader>
              <CardContent className="grid gap-4">
                <Field label="Status"><Select value={item.status} onChange={(event) => run(() => mutations.status.mutateAsync(event.target.value), "Status updated")}>{["NEW", "IN_REVIEW", "RESOLVED", "ARCHIVED"].map((x) => <option key={x}>{x}</option>)}</Select></Field>
                <Field label="Priority"><Select value={item.priority} onChange={(event) => run(() => mutations.priority.mutateAsync(event.target.value), "Priority updated")}>{["LOW", "MEDIUM", "HIGH", "CRITICAL"].map((x) => <option key={x}>{x}</option>)}</Select></Field>
                <form className="grid gap-2" onSubmit={(event) => { event.preventDefault(); run(() => mutations.assign.mutateAsync(assignedTo), "Owner saved"); }}>
                  <Field label="Assigned to"><Input value={assignedTo || item.assignedTo || ""} onChange={(event) => setAssignedTo(event.target.value)} /></Field>
                  <Button variant="secondary"><Save size={16} /> Save owner</Button>
                </form>
                <Button variant="secondary" onClick={() => run(() => item.status === "ARCHIVED" ? mutations.restore.mutateAsync() : mutations.archive.mutateAsync(), item.status === "ARCHIVED" ? "Feedback restored" : "Feedback archived")}><Archive size={16} /> {item.status === "ARCHIVED" ? "Restore" : "Archive"}</Button>
              </CardContent>
            </Card>
            <Card>
              <CardHeader><CardTitle>Customer context</CardTitle></CardHeader>
              <CardContent>
                {customer.data ? (
                  <Link className="block rounded-lg border border-slate-200 p-3 hover:bg-slate-50" to={`/app/customers/${customer.data.id}`}>
                    <strong>{customer.data.fullName}</strong>
                    <p className="text-sm text-slate-600">{customer.data.email}</p>
                  </Link>
                ) : <EmptyState icon={<FileText />} title="No customer linked" text="This feedback was created without a customer relationship." />}
              </CardContent>
            </Card>
          </div>
        </div>
      ) : <EmptyState icon={<FileText />} title="Feedback not found" text="The selected feedback could not be loaded." />}
    </>
  );
}
