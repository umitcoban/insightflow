import { Dispatch, FormEvent, ReactNode, SetStateAction, useMemo, useState } from "react";
import { Background, Controls, Handle, MiniMap, Node, Position, ReactFlow } from "@xyflow/react";
import { AlertCircle, Braces, GitBranch, ListPlus, Play, Save, Trash2, Webhook, Zap } from "lucide-react";
import { Badge } from "../../../components/ui/badge";
import { Button } from "../../../components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "../../../components/ui/card";
import { EmptyState } from "../../../components/ui/empty-state";
import { Field, Input, Select, Textarea } from "../../../components/ui/input";
import { toMessage } from "../../../lib/errors";
import { prettyJson } from "../../../lib/json";
import { cn } from "../../../lib/utils";
import { AutomationRule, AutomationRuleInput } from "../api";
import "@xyflow/react/dist/style.css";

type ConditionOperator = "eq" | "neq" | "in" | "notIn" | "contains" | "exists" | "gt" | "gte" | "lt" | "lte";
type ConditionGroup = "all" | "any";
type ActionKind = "LOG" | "WEBHOOK";

type ConditionDraft = {
  id: string;
  path: string;
  op: ConditionOperator;
  value: string;
};

type ActionDraft = {
  id: string;
  type: ActionKind;
  message: string;
  url: string;
  method: string;
  headers: string;
  body: string;
  timeoutMs: string;
};

export type RuleDraft = {
  name: string;
  description: string;
  triggerEventType: string;
  priority: number;
  conditionGroup: ConditionGroup;
  conditions: ConditionDraft[];
  actions: ActionDraft[];
};

type ComposerProps = {
  initialDraft?: RuleDraft;
  submitLabel: string;
  submitIcon?: ReactNode;
  onSubmit: (input: AutomationRuleInput, draft: RuleDraft) => Promise<void>;
  onDryRun?: (input: AutomationRuleInput, payload: Record<string, unknown>) => Promise<boolean | void>;
  onReplay?: (input: AutomationRuleInput, payload: Record<string, unknown>) => Promise<void>;
  secondaryAction?: ReactNode;
};

const triggerEvents = [
  { value: "feedback.created", label: "Feedback created" },
  { value: "feedback.updated", label: "Feedback updated" },
  { value: "feedback.ai-analysis-completed", label: "AI analysis completed" }
];

const conditionPaths = [
  "sentiment",
  "riskLevel",
  "category",
  "priority",
  "status",
  "source",
  "title",
  "content",
  "customerId",
  "assignedTo",
  "metadata.plan",
  "metadata.platform"
];

const operators: Array<{ value: ConditionOperator; label: string; needsValue: boolean }> = [
  { value: "eq", label: "is", needsValue: true },
  { value: "neq", label: "is not", needsValue: true },
  { value: "in", label: "is one of", needsValue: true },
  { value: "notIn", label: "is not one of", needsValue: true },
  { value: "contains", label: "contains", needsValue: true },
  { value: "exists", label: "exists", needsValue: false },
  { value: "gt", label: "greater than", needsValue: true },
  { value: "gte", label: "greater or equal", needsValue: true },
  { value: "lt", label: "less than", needsValue: true },
  { value: "lte", label: "less or equal", needsValue: true }
];

const defaultPayload = `{
  "sentiment": "NEGATIVE",
  "riskLevel": "HIGH",
  "priority": "CRITICAL",
  "source": "MANUAL",
  "category": "Billing"
}`;

export function AutomationRuleComposer({
  initialDraft = createDefaultRuleDraft(),
  submitLabel,
  submitIcon = <Save size={16} />,
  onSubmit,
  onDryRun,
  onReplay,
  secondaryAction
}: ComposerProps) {
  const [draft, setDraft] = useState(initialDraft);
  const [payload, setPayload] = useState(defaultPayload);
  const [busy, setBusy] = useState<"save" | "dryRun" | "replay" | null>(null);
  const [result, setResult] = useState<string | null>(null);
  const generated = useMemo(() => buildAutomationRuleInput(draft), [draft]);
  const validation = useMemo(() => validateDraft(draft), [draft]);
  const flow = useMemo(() => buildFlow(draft), [draft]);

  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (validation.length) {
      setResult(validation[0]);
      return;
    }
    setBusy("save");
    setResult(null);
    try {
      await onSubmit(generated, draft);
    } catch (error) {
      setResult(toMessage(error));
    } finally {
      setBusy(null);
    }
  }

  async function runTool(kind: "dryRun" | "replay") {
    if (validation.length) {
      setResult(validation[0]);
      return;
    }
    setBusy(kind);
    setResult(null);
    try {
      const parsedPayload = JSON.parse(payload) as Record<string, unknown>;
      if (!parsedPayload || Array.isArray(parsedPayload) || typeof parsedPayload !== "object") {
        throw new Error("Sample payload must be a JSON object.");
      }
      if (kind === "dryRun" && onDryRun) {
        const matched = await onDryRun(generated, parsedPayload);
        setResult(typeof matched === "boolean" ? `Dry-run ${matched ? "matched" : "did not match"}.` : "Dry-run completed.");
      }
      if (kind === "replay" && onReplay) {
        await onReplay(generated, parsedPayload);
        setResult("Replay accepted.");
      }
    } catch (error) {
      setResult(toMessage(error));
    } finally {
      setBusy(null);
    }
  }

  return (
    <form className="grid gap-5" onSubmit={submit}>
      <Card className="border-teal-100 bg-teal-50/40">
        <CardContent className="grid gap-3 md:grid-cols-[1fr_auto] md:items-center">
          <div>
            <p className="text-xs font-bold uppercase text-teal-800">Workflow sentence</p>
            <p className="mt-2 text-lg font-semibold text-slate-950">
              When <span className="text-teal-800">{triggerLabel(draft.triggerEventType)}</span> happens, if{" "}
              <span className="text-teal-800">{draft.conditionGroup === "all" ? "all" : "any"}</span> conditions match, then run{" "}
              <span className="text-teal-800">{draft.actions.length}</span> action{draft.actions.length === 1 ? "" : "s"}.
            </p>
          </div>
          <div className="flex flex-wrap gap-2">
            <Button disabled={busy === "save"}>{submitIcon}{busy === "save" ? "Saving..." : submitLabel}</Button>
            {secondaryAction}
          </div>
        </CardContent>
      </Card>

      <div className="grid gap-5 xl:grid-cols-[minmax(0,1fr)_430px]">
        <div className="grid gap-5">
          <Card>
            <CardHeader>
              <CardTitle>When</CardTitle>
              <Badge>Trigger</Badge>
            </CardHeader>
            <CardContent className="grid gap-4 md:grid-cols-[1fr_160px]">
              <Field label="Event">
                <Select value={draft.triggerEventType} onChange={(event) => patchDraft(setDraft, { triggerEventType: event.target.value })}>
                  {triggerEvents.map((trigger) => <option key={trigger.value} value={trigger.value}>{trigger.label}</option>)}
                </Select>
              </Field>
              <Field label="Priority">
                <Input type="number" value={draft.priority} onChange={(event) => patchDraft(setDraft, { priority: Number(event.target.value) })} />
              </Field>
              <Field label="Rule name">
                <Input value={draft.name} onChange={(event) => patchDraft(setDraft, { name: event.target.value })} />
              </Field>
              <Field label="Description">
                <Input value={draft.description} onChange={(event) => patchDraft(setDraft, { description: event.target.value })} />
              </Field>
            </CardContent>
          </Card>

          <Card>
            <CardHeader>
              <CardTitle>If</CardTitle>
              <div className="flex items-center gap-2">
                <Select className="h-9" value={draft.conditionGroup} onChange={(event) => patchDraft(setDraft, { conditionGroup: event.target.value as ConditionGroup })}>
                  <option value="all">All conditions</option>
                  <option value="any">Any condition</option>
                </Select>
                <Button type="button" size="sm" variant="secondary" onClick={() => patchDraft(setDraft, { conditions: [...draft.conditions, createCondition()] })}>
                  <ListPlus size={16} /> Add
                </Button>
              </div>
            </CardHeader>
            <CardContent className="grid gap-3">
              {draft.conditions.map((condition, index) => (
                <ConditionRow
                  key={condition.id}
                  index={index}
                  condition={condition}
                  onChange={(next) => updateCondition(setDraft, condition.id, next)}
                  onRemove={() => patchDraft(setDraft, { conditions: draft.conditions.filter((item) => item.id !== condition.id) })}
                />
              ))}
            </CardContent>
          </Card>

          <Card>
            <CardHeader>
              <CardTitle>Then</CardTitle>
              <Button type="button" size="sm" variant="secondary" onClick={() => patchDraft(setDraft, { actions: [...draft.actions, createAction("LOG")] })}>
                <ListPlus size={16} /> Add action
              </Button>
            </CardHeader>
            <CardContent className="grid gap-3">
              {draft.actions.map((action, index) => (
                <ActionRow
                  key={action.id}
                  index={index}
                  action={action}
                  onChange={(next) => updateAction(setDraft, action.id, next)}
                  onRemove={() => patchDraft(setDraft, { actions: draft.actions.filter((item) => item.id !== action.id) })}
                />
              ))}
            </CardContent>
          </Card>
        </div>

        <div className="grid content-start gap-5">
          <Card className="overflow-hidden">
            <CardHeader>
              <CardTitle>Workflow chart</CardTitle>
              <Badge>Live preview</Badge>
            </CardHeader>
            <div className="h-[360px]">
              <ReactFlow nodeTypes={nodeTypes} nodes={flow.nodes} edges={flow.edges} fitView minZoom={0.4} maxZoom={1.2}>
                <Background />
                <Controls />
                <MiniMap pannable zoomable />
              </ReactFlow>
            </div>
          </Card>

          <Card>
            <CardHeader>
              <CardTitle>Generated backend payload</CardTitle>
              <Braces size={18} className="text-slate-500" />
            </CardHeader>
            <CardContent>
              <Textarea readOnly className="min-h-80 font-mono text-xs" value={prettyJson(generated)} />
            </CardContent>
          </Card>

          {(onDryRun || onReplay) ? (
            <Card>
              <CardHeader><CardTitle>Test payload</CardTitle></CardHeader>
              <CardContent className="grid gap-3">
                <Textarea className="min-h-44 font-mono text-xs" value={payload} onChange={(event) => setPayload(event.target.value)} />
                <div className="flex flex-wrap gap-2">
                  {onDryRun ? <Button type="button" variant="secondary" disabled={busy === "dryRun"} onClick={() => runTool("dryRun")}><Play size={16} /> {busy === "dryRun" ? "Running..." : "Dry run"}</Button> : null}
                  {onReplay ? <Button type="button" variant="secondary" disabled={busy === "replay"} onClick={() => runTool("replay")}><Zap size={16} /> {busy === "replay" ? "Replaying..." : "Replay"}</Button> : null}
                </div>
                {result ? <div className={cn("rounded-lg border p-3 text-sm", result.includes("failed") || result.includes("must") ? "border-red-200 bg-red-50 text-red-700" : "border-teal-200 bg-teal-50 text-teal-800")}>{result}</div> : null}
              </CardContent>
            </Card>
          ) : null}

          {validation.length ? (
            <EmptyState icon={<AlertCircle />} title="Rule needs attention" text={validation[0]} />
          ) : null}
        </div>
      </div>
    </form>
  );
}

function ConditionRow({ condition, index, onChange, onRemove }: { condition: ConditionDraft; index: number; onChange: (condition: ConditionDraft) => void; onRemove: () => void }) {
  const operator = operators.find((item) => item.value === condition.op) ?? operators[0];
  return (
    <div className="grid gap-3 rounded-lg border border-slate-200 bg-white p-3 lg:grid-cols-[34px_1fr_150px_1fr_40px] lg:items-end">
      <div className="grid h-9 w-9 place-items-center rounded-lg bg-slate-100 text-sm font-bold text-slate-600">{index + 1}</div>
      <Field label="Field">
        <Select value={condition.path} onChange={(event) => onChange({ ...condition, path: event.target.value })}>
          {conditionPaths.map((path) => <option key={path} value={path}>{path}</option>)}
        </Select>
      </Field>
      <Field label="Operator">
        <Select value={condition.op} onChange={(event) => onChange({ ...condition, op: event.target.value as ConditionOperator, value: event.target.value === "exists" ? "true" : condition.value })}>
          {operators.map((op) => <option key={op.value} value={op.value}>{op.label}</option>)}
        </Select>
      </Field>
      {operator.needsValue ? (
        <Field label={condition.op === "in" || condition.op === "notIn" ? "Values, comma separated" : "Value"}>
          <Input value={condition.value} onChange={(event) => onChange({ ...condition, value: event.target.value })} />
        </Field>
      ) : (
        <Field label="Expectation">
          <Select value={condition.value} onChange={(event) => onChange({ ...condition, value: event.target.value })}>
            <option value="true">exists</option>
            <option value="false">does not exist</option>
          </Select>
        </Field>
      )}
      <Button type="button" size="icon" variant="ghost" aria-label="Remove condition" onClick={onRemove}><Trash2 size={16} /></Button>
    </div>
  );
}

function ActionRow({ action, index, onChange, onRemove }: { action: ActionDraft; index: number; onChange: (action: ActionDraft) => void; onRemove: () => void }) {
  return (
    <div className="grid gap-3 rounded-lg border border-slate-200 bg-white p-3">
      <div className="flex items-center justify-between gap-3">
        <div className="flex items-center gap-2">
          <div className="grid h-9 w-9 place-items-center rounded-lg bg-slate-100 text-sm font-bold text-slate-600">{index + 1}</div>
          <Field label="Action type">
            <Select value={action.type} onChange={(event) => onChange({ ...action, type: event.target.value as ActionKind })}>
              <option value="LOG">Log</option>
              <option value="WEBHOOK">Webhook</option>
            </Select>
          </Field>
        </div>
        <Button type="button" size="icon" variant="ghost" aria-label="Remove action" onClick={onRemove}><Trash2 size={16} /></Button>
      </div>
      {action.type === "LOG" ? (
        <Field label="Message">
          <Input value={action.message} onChange={(event) => onChange({ ...action, message: event.target.value })} />
        </Field>
      ) : (
        <div className="grid gap-3">
          <div className="grid gap-3 md:grid-cols-[1fr_130px_130px]">
            <Field label="URL"><Input value={action.url} onChange={(event) => onChange({ ...action, url: event.target.value })} /></Field>
            <Field label="Method">
              <Select value={action.method} onChange={(event) => onChange({ ...action, method: event.target.value })}>
                <option value="POST">POST</option>
                <option value="PUT">PUT</option>
                <option value="PATCH">PATCH</option>
                <option value="GET">GET</option>
              </Select>
            </Field>
            <Field label="Timeout ms"><Input value={action.timeoutMs} onChange={(event) => onChange({ ...action, timeoutMs: event.target.value })} /></Field>
          </div>
          <div className="grid gap-3 md:grid-cols-2">
            <Field label="Headers JSON"><Textarea className="font-mono text-xs" value={action.headers} onChange={(event) => onChange({ ...action, headers: event.target.value })} /></Field>
            <Field label="Body JSON"><Textarea className="font-mono text-xs" value={action.body} onChange={(event) => onChange({ ...action, body: event.target.value })} /></Field>
          </div>
        </div>
      )}
    </div>
  );
}

export function createDefaultRuleDraft(): RuleDraft {
  return {
    name: "Escalate risky negative feedback",
    description: "When AI enrichment marks feedback as high risk, record an automation action.",
    triggerEventType: "feedback.ai-analysis-completed",
    priority: 100,
    conditionGroup: "all",
    conditions: [
      { id: cryptoId(), path: "sentiment", op: "eq", value: "NEGATIVE" },
      { id: cryptoId(), path: "riskLevel", op: "in", value: "HIGH,CRITICAL" }
    ],
    actions: [createAction("LOG")]
  };
}

export function draftFromRule(rule: AutomationRule): RuleDraft {
  const group = Array.isArray(rule.conditionJson.any) ? "any" : "all";
  const rawConditions = Array.isArray(rule.conditionJson[group]) ? rule.conditionJson[group] as Array<Record<string, unknown>> : [];
  return {
    name: rule.name,
    description: rule.description ?? "",
    triggerEventType: rule.triggerEventType,
    priority: rule.priority,
    conditionGroup: group,
    conditions: rawConditions.length ? rawConditions.map(conditionFromJson) : [createCondition()],
    actions: rule.actionJson.length ? rule.actionJson.map(actionFromJson) : [createAction("LOG")]
  };
}

export function buildAutomationRuleInput(draft: RuleDraft): AutomationRuleInput {
  return {
    name: draft.name.trim(),
    description: draft.description.trim(),
    triggerEventType: draft.triggerEventType,
    priority: draft.priority,
    conditionJson: {
      [draft.conditionGroup]: draft.conditions.map((condition) => ({
        path: condition.path,
        op: condition.op,
        value: parseConditionValue(condition)
      }))
    },
    actionJson: draft.actions.map(actionToJson)
  };
}

function validateDraft(draft: RuleDraft) {
  const errors: string[] = [];
  if (!draft.name.trim()) errors.push("Rule name is required.");
  if (!draft.triggerEventType.trim()) errors.push("Trigger event is required.");
  if (!draft.conditions.length) errors.push("At least one condition is required.");
  if (!draft.actions.length) errors.push("At least one action is required.");
  draft.conditions.forEach((condition) => {
    if (!condition.path.trim()) errors.push("Every condition needs a field.");
    if (operators.find((op) => op.value === condition.op)?.needsValue && !condition.value.trim()) {
      errors.push(`${condition.path} condition needs a value.`);
    }
  });
  draft.actions.forEach((action) => {
    if (action.type === "LOG" && !action.message.trim()) errors.push("Log action needs a message.");
    if (action.type === "WEBHOOK" && !action.url.trim()) errors.push("Webhook action needs a URL.");
    if (action.type === "WEBHOOK") {
      tryParseJsonObject(action.headers, "Headers JSON", errors);
      tryParseJson(action.body, "Body JSON", errors);
    }
  });
  return errors;
}

function actionToJson(action: ActionDraft) {
  if (action.type === "LOG") {
    return { type: "LOG", message: action.message.trim() };
  }
  return {
    type: "WEBHOOK",
    url: action.url.trim(),
    method: action.method,
    headers: parseJsonOrFallback(action.headers, {}),
    body: parseJsonOrFallback(action.body, {}),
    timeoutMs: Number(action.timeoutMs || 5000)
  };
}

function conditionFromJson(condition: Record<string, unknown>): ConditionDraft {
  return {
    id: cryptoId(),
    path: String(condition.path ?? "sentiment"),
    op: operators.some((operator) => operator.value === condition.op) ? condition.op as ConditionOperator : "eq",
    value: valueToInput(condition.value)
  };
}

function actionFromJson(action: Record<string, unknown>): ActionDraft {
  const type = action.type === "WEBHOOK" ? "WEBHOOK" : "LOG";
  return {
    id: cryptoId(),
    type,
    message: String(action.message ?? "Automation matched"),
    url: String(action.url ?? "https://example.com/webhook"),
    method: String(action.method ?? "POST"),
    headers: prettyJson(action.headers ?? {}),
    body: prettyJson(action.body ?? {}),
    timeoutMs: String(action.timeoutMs ?? 5000)
  };
}

function createCondition(): ConditionDraft {
  return { id: cryptoId(), path: "priority", op: "in", value: "HIGH,CRITICAL" };
}

function createAction(type: ActionKind): ActionDraft {
  return {
    id: cryptoId(),
    type,
    message: "Automation matched",
    url: "https://example.com/webhook",
    method: "POST",
    headers: "{}",
    body: "{\n  \"eventId\": \"{{eventId}}\",\n  \"eventType\": \"{{eventType}}\"\n}",
    timeoutMs: "5000"
  };
}

function parseConditionValue(condition: ConditionDraft) {
  if (condition.op === "exists") {
    return condition.value !== "false";
  }
  if (condition.op === "in" || condition.op === "notIn") {
    return condition.value.split(",").map((value) => parseScalar(value.trim())).filter((value) => value !== "");
  }
  return parseScalar(condition.value.trim());
}

function parseScalar(value: string) {
  if (value === "true") return true;
  if (value === "false") return false;
  if (value === "null") return null;
  if (value !== "" && !Number.isNaN(Number(value)) && /^-?\d+(\.\d+)?$/.test(value)) return Number(value);
  return value;
}

function parseJsonOrFallback(value: string, fallback: unknown) {
  if (!value.trim()) return fallback;
  return JSON.parse(value);
}

function tryParseJson(value: string, label: string, errors: string[]) {
  try {
    parseJsonOrFallback(value, {});
  } catch {
    errors.push(`${label} is not valid JSON.`);
  }
}

function tryParseJsonObject(value: string, label: string, errors: string[]) {
  try {
    const parsed = parseJsonOrFallback(value, {});
    if (!parsed || Array.isArray(parsed) || typeof parsed !== "object") {
      errors.push(`${label} must be a JSON object.`);
    }
  } catch {
    errors.push(`${label} is not valid JSON.`);
  }
}

function valueToInput(value: unknown) {
  if (Array.isArray(value)) {
    return value.join(",");
  }
  if (value === undefined || value === null) {
    return "";
  }
  return String(value);
}

function patchDraft(setDraft: Dispatch<SetStateAction<RuleDraft>>, patch: Partial<RuleDraft>) {
  setDraft((current) => ({ ...current, ...patch }));
}

function updateCondition(setDraft: Dispatch<SetStateAction<RuleDraft>>, id: string, next: ConditionDraft) {
  setDraft((current) => ({ ...current, conditions: current.conditions.map((item) => item.id === id ? next : item) }));
}

function updateAction(setDraft: Dispatch<SetStateAction<RuleDraft>>, id: string, next: ActionDraft) {
  setDraft((current) => ({ ...current, actions: current.actions.map((item) => item.id === id ? next : item) }));
}

function triggerLabel(value: string) {
  return triggerEvents.find((event) => event.value === value)?.label ?? value;
}

function cryptoId() {
  return crypto.randomUUID();
}

type FlowNodeData = { title: string; subtitle: string; kind: "trigger" | "condition" | "action" };

const nodeTypes = {
  story: WorkflowNode
};

function WorkflowNode({ data }: { data: FlowNodeData }) {
  const Icon = data.kind === "trigger" ? Zap : data.kind === "condition" ? GitBranch : Webhook;
  return (
    <div className={cn(
      "min-w-56 rounded-lg border bg-white p-3 shadow-sm",
      data.kind === "trigger" && "border-teal-200",
      data.kind === "condition" && "border-amber-200",
      data.kind === "action" && "border-blue-200"
    )}>
      <Handle type="target" position={Position.Left} className="!bg-slate-400" />
      <div className="flex items-start gap-2">
        <div className="grid h-8 w-8 place-items-center rounded-lg bg-slate-100 text-slate-700"><Icon size={16} /></div>
        <div>
          <strong className="block text-sm text-slate-950">{data.title}</strong>
          <span className="text-xs text-slate-500">{data.subtitle}</span>
        </div>
      </div>
      <Handle type="source" position={Position.Right} className="!bg-slate-400" />
    </div>
  );
}

function buildFlow(draft: RuleDraft) {
  const nodes: Array<Node<FlowNodeData>> = [
    { id: "trigger", type: "story", position: { x: 0, y: 120 }, data: { title: "When", subtitle: triggerLabel(draft.triggerEventType), kind: "trigger" } },
    { id: "group", type: "story", position: { x: 280, y: 120 }, data: { title: `If ${draft.conditionGroup}`, subtitle: `${draft.conditions.length} condition${draft.conditions.length === 1 ? "" : "s"}`, kind: "condition" } },
    { id: "actions", type: "story", position: { x: 560, y: 120 }, data: { title: "Then", subtitle: draft.actions.map((action) => action.type).join(" + "), kind: "action" } }
  ];
  const edges = [
    { id: "trigger-group", source: "trigger", target: "group", animated: true },
    { id: "group-actions", source: "group", target: "actions", animated: true }
  ];
  return { nodes, edges };
}
