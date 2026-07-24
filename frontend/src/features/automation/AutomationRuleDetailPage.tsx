import { FormEvent } from "react";
import { useNavigate, useParams } from "react-router";
import { Play, Save, Trash2, Zap } from "lucide-react";
import { Badge } from "../../components/ui/badge";
import { Button } from "../../components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "../../components/ui/card";
import { EmptyState } from "../../components/ui/empty-state";
import { Field, Input, Textarea } from "../../components/ui/input";
import { PageHeader } from "../../components/ui/page-header";
import { useToast } from "../../components/ui/toast";
import { toMessage } from "../../lib/errors";
import { parseJsonArray, parseJsonObject, prettyJson } from "../../lib/json";
import { useAutomationMutations, useAutomationRule } from "./hooks";

const defaultPayload = '{\n  "sentiment": "NEGATIVE",\n  "riskLevel": "HIGH"\n}';

export function AutomationRuleDetailPage() {
  const { ruleId } = useParams();
  const navigate = useNavigate();
  const rule = useAutomationRule(ruleId);
  const mutations = useAutomationMutations(ruleId);
  const { notify } = useToast();

  async function save(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const form = new FormData(event.currentTarget);
    try {
      await mutations.update.mutateAsync({
        name: form.get("name")?.toString(),
        description: form.get("description")?.toString(),
        triggerEventType: form.get("triggerEventType")?.toString(),
        priority: Number(form.get("priority")?.toString() || 0),
        conditionJson: parseJsonObject(form.get("conditionJson")?.toString() ?? "{}", "Condition JSON"),
        actionJson: parseJsonArray(form.get("actionJson")?.toString() ?? "[]", "Action JSON")
      });
      notify("Rule saved");
    } catch (error) {
      notify("Save failed", toMessage(error));
    }
  }

  async function withToast(action: () => Promise<unknown>, success: string) {
    try {
      await action();
      notify(success);
    } catch (error) {
      notify(`${success} failed`, toMessage(error));
    }
  }

  const item = rule.data;
  return (
    <>
      <PageHeader eyebrow="Rule detail" title={item?.name ?? "Loading rule"} description="Edit automation behavior, test payload matching and replay execution." actions={item ? <Badge>{item.status}</Badge> : null} />
      {item ? (
        <div className="grid gap-5 xl:grid-cols-[1fr_420px]">
          <Card>
            <CardHeader><CardTitle>Definition</CardTitle></CardHeader>
            <CardContent>
              <form className="grid gap-4" onSubmit={save}>
                <div className="grid gap-4 md:grid-cols-2">
                  <Field label="Name"><Input name="name" defaultValue={item.name} required /></Field>
                  <Field label="Trigger event"><Input name="triggerEventType" defaultValue={item.triggerEventType} required /></Field>
                  <Field label="Priority"><Input name="priority" type="number" defaultValue={item.priority} /></Field>
                  <Field label="Description"><Input name="description" defaultValue={item.description ?? ""} /></Field>
                </div>
                <Field label="Condition JSON"><Textarea name="conditionJson" className="min-h-56 font-mono text-xs" defaultValue={prettyJson(item.conditionJson)} /></Field>
                <Field label="Action JSON"><Textarea name="actionJson" className="min-h-40 font-mono text-xs" defaultValue={prettyJson(item.actionJson)} /></Field>
                <div className="flex flex-wrap gap-2">
                  <Button><Save size={16} /> Save</Button>
                  {item.status === "ACTIVE" ? <Button type="button" variant="secondary" onClick={() => withToast(() => mutations.deactivate.mutateAsync(), "Rule deactivated")}>Deactivate</Button> : <Button type="button" onClick={() => withToast(() => mutations.activate.mutateAsync(), "Rule activated")}>Activate</Button>}
                  <Button type="button" variant="danger" onClick={() => withToast(async () => { await mutations.deleteRule.mutateAsync(); navigate("/app/automation"); }, "Rule deleted")}><Trash2 size={16} /> Delete</Button>
                </div>
              </form>
            </CardContent>
          </Card>
          <Card>
            <CardHeader><CardTitle>Payload tools</CardTitle></CardHeader>
            <CardContent className="grid gap-5">
              <PayloadTool label="Dry run" defaultValue={defaultPayload} onSubmit={(payload) => withToast(async () => {
                const result = await mutations.dryRun.mutateAsync(payload);
                notify("Dry-run result", result.matched ? "MATCHED" : "NOT MATCHED");
              }, "Dry run")} />
              <PayloadTool label="Replay" defaultValue={defaultPayload} onSubmit={(payload) => withToast(() => mutations.replay.mutateAsync(payload), "Replay accepted")} />
            </CardContent>
          </Card>
        </div>
      ) : <EmptyState icon={<Zap />} title="Rule not found" text="The selected automation rule could not be loaded." />}
    </>
  );
}

function PayloadTool({ label, defaultValue, onSubmit }: { label: string; defaultValue: string; onSubmit: (payload: Record<string, unknown>) => void }) {
  return (
    <form className="grid gap-3" onSubmit={(event) => {
      event.preventDefault();
      onSubmit(parseJsonObject(new FormData(event.currentTarget).get("payload")?.toString() ?? "{}", "Payload JSON"));
    }}>
      <Field label={`${label} payload`}><Textarea name="payload" className="font-mono text-xs" defaultValue={defaultValue} /></Field>
      <Button variant="secondary"><Play size={16} /> {label}</Button>
    </form>
  );
}
