import { FormEvent } from "react";
import { FileText, Plus, Trash2 } from "lucide-react";
import { Button } from "../../components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "../../components/ui/card";
import { EmptyState } from "../../components/ui/empty-state";
import { Field, Input, Textarea } from "../../components/ui/input";
import { PageHeader } from "../../components/ui/page-header";
import { useToast } from "../../components/ui/toast";
import { formatDate } from "../../lib/dates";
import { toMessage } from "../../lib/errors";
import { useKnowledgeDocuments, useKnowledgeMutations } from "./hooks";

export function KnowledgePage() {
  const documents = useKnowledgeDocuments();
  const mutations = useKnowledgeMutations();
  const { notify } = useToast();

  async function createDocument(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const formElement = event.currentTarget;
    const form = new FormData(formElement);
    try {
      await mutations.create.mutateAsync({
        title: form.get("title")?.toString(),
        source: form.get("source")?.toString(),
        content: form.get("content")?.toString()
      });
      formElement.reset();
      notify("Knowledge document indexed");
    } catch (error) {
      notify("Knowledge indexing failed", toMessage(error));
    }
  }

  return (
    <>
      <PageHeader eyebrow="Knowledge" title="Tenant knowledge base" description="Index operational policy, support playbooks and product context for the assistant." />
      <div className="grid gap-5 xl:grid-cols-[1fr_420px]">
        <Card>
          <CardHeader><CardTitle>Documents</CardTitle><span className="text-sm text-slate-500">{documents.data?.totalElements ?? 0} indexed</span></CardHeader>
          <CardContent className="grid gap-3">
            {documents.data?.content.length ? documents.data.content.map((document) => (
              <div key={document.id} className="flex items-center justify-between gap-3 rounded-lg border border-slate-200 p-4">
                <div><strong>{document.title}</strong><p className="text-sm text-slate-600">{document.source} · {formatDate(document.createdAt)}</p></div>
                <Button variant="danger" size="icon" onClick={() => mutations.deleteDocument.mutate(document.id)}><Trash2 size={16} /></Button>
              </div>
            )) : <EmptyState icon={<FileText />} title="No documents" text="Add knowledge before asking assistant questions." />}
          </CardContent>
        </Card>
        <Card>
          <CardHeader><CardTitle>Add Knowledge</CardTitle></CardHeader>
          <CardContent>
            <form className="grid gap-4" onSubmit={createDocument}>
              <Field label="Title"><Input name="title" required /></Field>
              <Field label="Source"><Input name="source" required placeholder="support-playbook" /></Field>
              <Field label="Content"><Textarea name="content" required className="min-h-56" /></Field>
              <Button><Plus size={16} /> Index document</Button>
            </form>
          </CardContent>
        </Card>
      </div>
    </>
  );
}
