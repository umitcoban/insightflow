import { FormEvent, useMemo, useState } from "react";
import { useNavigate } from "react-router";
import { Background, Controls, MiniMap, Node, ReactFlow, ReactFlowProvider, useEdgesState, useNodesState } from "@xyflow/react";
import { GitBranch, Play, Plus, Save, Webhook, Zap } from "lucide-react";
import { Button } from "../../../components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "../../../components/ui/card";
import { Field, Input, Select, Textarea } from "../../../components/ui/input";
import { PageHeader } from "../../../components/ui/page-header";
import { useToast } from "../../../components/ui/toast";
import { useAuth } from "../../../auth/AuthProvider";
import { toMessage } from "../../../lib/errors";
import { parseJsonObject, prettyJson } from "../../../lib/json";
import { automationApi } from "../api";
import { useAutomationMutations } from "../hooks";
import "@xyflow/react/dist/style.css";

type NodeKind = "trigger" | "conditionGroup" | "condition" | "logAction" | "webhookAction";
type BuilderNode = Node<{ label: string; kind: NodeKind; payload: Record<string, unknown> }>;

const starterNodes: BuilderNode[] = [
  { id: "trigger", type: "default", position: { x: 80, y: 80 }, data: { label: "Trigger: feedback.ai-analysis-completed", kind: "trigger", payload: { triggerEventType: "feedback.ai-analysis-completed" } } },
  { id: "group", type: "default", position: { x: 360, y: 80 }, data: { label: "Condition group: all", kind: "conditionGroup", payload: { group: "all" } } },
  { id: "condition-1", type: "default", position: { x: 640, y: 80 }, data: { label: "sentiment eq NEGATIVE", kind: "condition", payload: { path: "sentiment", op: "eq", value: "NEGATIVE" } } },
  { id: "action-1", type: "default", position: { x: 920, y: 80 }, data: { label: "LOG action", kind: "logAction", payload: { type: "LOG", message: "Negative feedback detected" } } }
];

const starterEdges = [
  { id: "e1", source: "trigger", target: "group" },
  { id: "e2", source: "group", target: "condition-1" },
  { id: "e3", source: "condition-1", target: "action-1" }
];

export function AutomationPlaygroundPage() {
  return (
    <ReactFlowProvider>
      <AutomationPlayground />
    </ReactFlowProvider>
  );
}

function AutomationPlayground() {
  const navigate = useNavigate();
  const { session } = useAuth();
  const { notify } = useToast();
  const mutations = useAutomationMutations();
  const [nodes, setNodes, onNodesChange] = useNodesState(starterNodes);
  const [edges, setEdges, onEdgesChange] = useEdgesState(starterEdges);
  const [name, setName] = useState("Playground generated rule");
  const [priority, setPriority] = useState(100);
  const [payload, setPayload] = useState('{\n  "sentiment": "NEGATIVE",\n  "riskLevel": "HIGH"\n}');
  const [draftNode, setDraftNode] = useState<NodeKind>("condition");

  const generated = useMemo(() => generateRule(nodes as BuilderNode[]), [nodes]);

  function addNode() {
    const id = `${draftNode}-${Date.now()}`;
    setNodes((current) => [...current, {
      id,
      type: "default",
      position: { x: 120 + current.length * 48, y: 180 + current.length * 26 },
      data: defaultNodeData(draftNode)
    }]);
  }

  async function saveRule() {
    try {
      const rule = await mutations.create.mutateAsync({
        name,
        description: "Generated from automation playground.",
        triggerEventType: generated.triggerEventType,
        priority,
        conditionJson: generated.conditionJson,
        actionJson: generated.actionJson
      });
      notify("Rule saved from playground");
      navigate(`/app/automation/${rule.id}`);
    } catch (error) {
      notify("Save failed", toMessage(error));
    }
  }

  async function dryRun(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    try {
      const parsedPayload = parseJsonObject(payload, "Sample payload");
      const tempRule = await mutations.create.mutateAsync({
        name: `${name} dry-run`,
        description: "Temporary playground dry-run rule.",
        triggerEventType: generated.triggerEventType,
        priority,
        conditionJson: generated.conditionJson,
        actionJson: generated.actionJson
      });
      const result = await automationApi.dryRun(session!, tempRule.id, parsedPayload);
      await automationApi.delete(session!, tempRule.id).catch(() => undefined);
      notify("Dry-run result", result.matched ? "MATCHED" : "NOT MATCHED");
    } catch (error) {
      notify("Dry-run failed", toMessage(error));
    }
  }

  return (
    <>
      <PageHeader eyebrow="Automation playground" title="Build automation visually" description="Compose trigger, condition and action nodes, then save the generated backend rule." />
      <div className="grid gap-5 xl:grid-cols-[280px_1fr_420px]">
        <Card>
          <CardHeader><CardTitle>Palette</CardTitle></CardHeader>
          <CardContent className="grid gap-4">
            <Field label="Node type">
              <Select value={draftNode} onChange={(event) => setDraftNode(event.target.value as NodeKind)}>
                <option value="trigger">Trigger</option>
                <option value="conditionGroup">Condition group</option>
                <option value="condition">Condition</option>
                <option value="logAction">Log action</option>
                <option value="webhookAction">Webhook action</option>
              </Select>
            </Field>
            <Button onClick={addNode}><Plus size={16} /> Add node</Button>
            <div className="grid gap-2 text-sm text-slate-600">
              <Hint icon={<Zap size={16} />} text="Trigger defines the domain event." />
              <Hint icon={<GitBranch size={16} />} text="Conditions map to all/any DSL." />
              <Hint icon={<Webhook size={16} />} text="Actions generate LOG or WEBHOOK JSON." />
            </div>
          </CardContent>
        </Card>
        <Card className="overflow-hidden">
          <div className="h-[640px]">
            <ReactFlow nodes={nodes} edges={edges} onNodesChange={onNodesChange} onEdgesChange={onEdgesChange} fitView>
              <Background />
              <Controls />
              <MiniMap />
            </ReactFlow>
          </div>
        </Card>
        <div className="grid content-start gap-5">
          <Card>
            <CardHeader><CardTitle>Rule metadata</CardTitle></CardHeader>
            <CardContent className="grid gap-4">
              <Field label="Name"><Input value={name} onChange={(event) => setName(event.target.value)} /></Field>
              <Field label="Priority"><Input type="number" value={priority} onChange={(event) => setPriority(Number(event.target.value))} /></Field>
              <Button onClick={saveRule}><Save size={16} /> Save Rule</Button>
            </CardContent>
          </Card>
          <Card>
            <CardHeader><CardTitle>Generated JSON</CardTitle></CardHeader>
            <CardContent className="grid gap-4">
              <Textarea readOnly className="min-h-80 font-mono text-xs" value={prettyJson(generated)} />
              <form className="grid gap-3" onSubmit={dryRun}>
                <Field label="Sample payload"><Textarea className="font-mono text-xs" value={payload} onChange={(event) => setPayload(event.target.value)} /></Field>
                <Button variant="secondary"><Play size={16} /> Prepare dry-run</Button>
              </form>
            </CardContent>
          </Card>
        </div>
      </div>
    </>
  );
}

function defaultNodeData(kind: NodeKind) {
  if (kind === "trigger") return { label: "Trigger: feedback.ai-analysis-completed", kind, payload: { triggerEventType: "feedback.ai-analysis-completed" } };
  if (kind === "conditionGroup") return { label: "Condition group: all", kind, payload: { group: "all" } };
  if (kind === "logAction") return { label: "LOG action", kind, payload: { type: "LOG", message: "Automation matched" } };
  if (kind === "webhookAction") return { label: "WEBHOOK action", kind, payload: { type: "WEBHOOK", url: "https://example.com/webhook", method: "POST" } };
  return { label: "priority in HIGH, CRITICAL", kind, payload: { path: "priority", op: "in", value: ["HIGH", "CRITICAL"] } };
}

function generateRule(nodes: BuilderNode[]) {
  const trigger = nodes.find((node) => node.data.kind === "trigger")?.data.payload.triggerEventType?.toString() ?? "feedback.ai-analysis-completed";
  const group = nodes.find((node) => node.data.kind === "conditionGroup")?.data.payload.group?.toString() === "any" ? "any" : "all";
  const conditions = nodes.filter((node) => node.data.kind === "condition").map((node) => node.data.payload);
  const actions = nodes
    .filter((node) => node.data.kind === "logAction" || node.data.kind === "webhookAction")
    .map((node) => node.data.payload);
  return {
    triggerEventType: trigger,
    conditionJson: { [group]: conditions.length ? conditions : [{ path: "sentiment", op: "eq", value: "NEGATIVE" }] },
    actionJson: actions.length ? actions : [{ type: "LOG", message: "Automation matched" }]
  };
}

function Hint({ icon, text }: { icon: React.ReactNode; text: string }) {
  return <div className="flex gap-2 rounded-lg bg-slate-50 p-3">{icon}<span>{text}</span></div>;
}
