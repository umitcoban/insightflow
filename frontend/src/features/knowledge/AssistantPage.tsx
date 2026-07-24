import { FormEvent } from "react";
import { Bot, ExternalLink } from "lucide-react";
import { Link } from "react-router";
import { Button } from "../../components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "../../components/ui/card";
import { EmptyState } from "../../components/ui/empty-state";
import { Input } from "../../components/ui/input";
import { PageHeader } from "../../components/ui/page-header";
import { useToast } from "../../components/ui/toast";
import { toMessage } from "../../lib/errors";
import { useKnowledgeMutations } from "./hooks";

export function AssistantPage() {
  const mutations = useKnowledgeMutations();
  const { notify } = useToast();

  async function ask(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const question = new FormData(event.currentTarget).get("question")?.toString() ?? "";
    try {
      await mutations.ask.mutateAsync(question);
    } catch (error) {
      notify("Assistant unavailable", toMessage(error));
    }
  }

  const answer = mutations.ask.data;
  return (
    <>
      <PageHeader eyebrow="Assistant" title="Tenant knowledge assistant" description="Ask questions answered only from indexed tenant documents." actions={<Link to="/app/knowledge"><Button variant="secondary"><ExternalLink size={16} /> Manage knowledge</Button></Link>} />
      <Card>
        <CardContent>
          <form className="flex flex-col gap-3 md:flex-row" onSubmit={ask}>
            <Input name="question" required placeholder="Can we refund App Store purchases directly?" />
            <Button disabled={mutations.ask.isPending}>{mutations.ask.isPending ? "Thinking..." : "Ask"}</Button>
          </form>
        </CardContent>
      </Card>
      <Card>
        <CardHeader><CardTitle>Answer</CardTitle></CardHeader>
        <CardContent>
          {answer ? (
            <div className="grid gap-5">
              <p className="text-sm leading-7 text-slate-700">{answer.answer}</p>
              <div className="grid gap-3">
                {answer.sources?.map((source) => (
                  <div key={source.chunkId} className="rounded-lg border border-slate-200 p-3">
                    <strong>{source.documentTitle}</strong>
                    <p className="mt-1 text-sm text-slate-600">{source.content}</p>
                  </div>
                ))}
              </div>
            </div>
          ) : <EmptyState icon={<Bot />} title="No question yet" text="Ask a tenant-specific question to retrieve an AI answer and citations." />}
        </CardContent>
      </Card>
    </>
  );
}
