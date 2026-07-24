import { FormEvent, useState } from "react";
import { Link, useNavigate } from "react-router";
import { Plus, Workflow, Zap } from "lucide-react";
import { Badge } from "../../components/ui/badge";
import { Button } from "../../components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "../../components/ui/card";
import { EmptyState } from "../../components/ui/empty-state";
import { Field, Input, Textarea } from "../../components/ui/input";
import { PageHeader } from "../../components/ui/page-header";
import { useToast } from "../../components/ui/toast";
import { toMessage } from "../../lib/errors";
import { parseJsonArray, parseJsonObject, prettyJson } from "../../lib/json";
import { AutomationExecution } from "./api";
import { useAutomationActionExecutions, useAutomationExecutions, useAutomationMutations, useAutomationRules } from "./hooks";

const defaultCondition = '{\n  "all": [\n    { "path": "sentiment", "op": "eq", "value": "NEGATIVE" }\n  ]\n}';
const defaultActions = '[\n  { "type": "LOG", "message": "Negative feedback detected" }\n]';

export function AutomationRulesPage() {
  const navigate = useNavigate();
  const [selectedExecution, setSelectedExecution] = useState<AutomationExecution | null>(null);
  const rules = useAutomationRules();
  const executions = useAutomationExecutions();
  const actions = useAutomationActionExecutions(selectedExecution?.id);
  const mutations = useAutomationMutations();
  const { notify } = useToast();

  async function createRule(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const form = new FormData(event.currentTarget);
    try {
      const rule = await mutations.create.mutateAsync({
        name: form.get("name")?.toString(),
        description: form.get("description")?.toString(),
        triggerEventType: form.get("triggerEventType")?.toString(),
        priority: Number(form.get("priority")?.toString() || 0),
        conditionJson: parseJsonObject(form.get("conditionJson")?.toString() ?? "{}", "Condition JSON"),
        actionJson: parseJsonArray(form.get("actionJson")?.toString() ?? "[]", "Action JSON")
      });
      notify("Automation rule created");
      navigate(`/app/automation/${rule.id}`);
    } catch (error) {
      notify("Automation rule failed", toMessage(error));
    }
  }

  return (
    <>
      <PageHeader eyebrow="Automation" title="Rules and executions" description="Create event-driven controls, replay payloads and inspect execution history." actions={<Link to="/app/automation/playground"><Button><Workflow size={16} /> Open playground</Button></Link>} />
      <div className="grid gap-5 xl:grid-cols-[1fr_420px]">
        <div className="grid gap-5">
          <Card>
            <CardHeader><CardTitle>Rules</CardTitle></CardHeader>
            <CardContent className="grid gap-3">
              {rules.data?.content.length ? rules.data.content.map((rule) => (
                <Link key={rule.id} to={`/app/automation/${rule.id}`} className="flex items-center justify-between gap-3 rounded-lg border border-slate-200 p-4 hover:bg-slate-50">
                  <div><strong>{rule.name}</strong><p className="text-sm text-slate-600">{rule.triggerEventType} · priority {rule.priority}</p></div>
                  <Badge>{rule.status}</Badge>
                </Link>
              )) : <EmptyState icon={<Zap />} title="No rules" text="Create a rule manually or use the playground." />}
            </CardContent>
          </Card>
          <Card>
            <CardHeader><CardTitle>Recent executions</CardTitle></CardHeader>
            <CardContent className="grid gap-3">
              {executions.data?.content.length ? executions.data.content.map((execution) => (
                <button key={execution.id} onClick={() => setSelectedExecution(execution)} className="flex items-center justify-between rounded-lg border border-slate-200 p-3 text-left hover:bg-slate-50">
                  <span className="text-sm">{execution.sourceEventType}</span>
                  <Badge>{execution.status}</Badge>
                </button>
              )) : <EmptyState icon={<Zap />} title="No executions" text="Executions appear after matching domain events or replay." />}
            </CardContent>
          </Card>
        </div>
        <div className="grid content-start gap-5">
          <Card>
            <CardHeader><CardTitle>Create Rule</CardTitle></CardHeader>
            <CardContent>
              <form className="grid gap-4" onSubmit={createRule}>
                <Field label="Name"><Input name="name" required /></Field>
                <Field label="Description"><Input name="description" /></Field>
                <Field label="Trigger event"><Input name="triggerEventType" required defaultValue="feedback.ai-analysis-completed" /></Field>
                <Field label="Priority"><Input name="priority" type="number" defaultValue="100" /></Field>
                <Field label="Condition JSON"><Textarea name="conditionJson" className="font-mono text-xs" defaultValue={defaultCondition} /></Field>
                <Field label="Action JSON"><Textarea name="actionJson" className="font-mono text-xs" defaultValue={defaultActions} /></Field>
                <Button><Plus size={16} /> Create rule</Button>
              </form>
            </CardContent>
          </Card>
          <Card>
            <CardHeader><CardTitle>Execution actions</CardTitle>{selectedExecution ? <Badge>{selectedExecution.status}</Badge> : null}</CardHeader>
            <CardContent className="grid gap-3">
              {selectedExecution ? (
                actions.data?.length ? actions.data.map((action) => (
                  <div key={action.id} className="rounded-lg border border-slate-200 p-3">
                    <div className="flex items-center justify-between gap-2"><strong>{action.actionType}</strong><Badge>{action.status}</Badge></div>
                    {action.errorMessage ? <p className="mt-2 text-sm text-red-700">{action.errorMessage}</p> : null}
                    <pre className="mt-3 max-h-48 overflow-auto rounded bg-slate-50 p-2 text-xs text-slate-600">{prettyJson({ request: action.requestPayload, result: action.resultPayload, attempts: action.attemptCount })}</pre>
                  </div>
                )) : <EmptyState icon={<Zap />} title="No action details" text="This execution has no recorded action payloads yet." />
              ) : <EmptyState icon={<Zap />} title="Select execution" text="Click an execution to inspect action request/result details." />}
            </CardContent>
          </Card>
        </div>
      </div>
    </>
  );
}
