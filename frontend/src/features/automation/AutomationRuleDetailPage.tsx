import { useNavigate, useParams } from "react-router";
import { Save, Trash2, Zap } from "lucide-react";
import { Badge } from "../../components/ui/badge";
import { Button } from "../../components/ui/button";
import { EmptyState } from "../../components/ui/empty-state";
import { PageHeader } from "../../components/ui/page-header";
import { useToast } from "../../components/ui/toast";
import { toMessage } from "../../lib/errors";
import { AutomationRuleComposer, draftFromRule } from "./components/rule-composer";
import { useAutomationMutations, useAutomationRule } from "./hooks";

export function AutomationRuleDetailPage() {
  const { ruleId } = useParams();
  const navigate = useNavigate();
  const rule = useAutomationRule(ruleId);
  const mutations = useAutomationMutations(ruleId);
  const { notify } = useToast();

  async function save(input: Parameters<typeof mutations.update.mutateAsync>[0]) {
    try {
      await mutations.update.mutateAsync(input);
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
        <AutomationRuleComposer
          key={item.id}
          initialDraft={draftFromRule(item)}
          submitLabel="Save rule"
          submitIcon={<Save size={16} />}
          onSubmit={(input) => save(input)}
          onDryRun={async (_input, payload) => {
            const result = await mutations.dryRun.mutateAsync(payload);
            notify("Dry-run result", result.matched ? "MATCHED" : "NOT MATCHED");
            return result.matched;
          }}
          onReplay={async (_input, payload) => {
            await mutations.replay.mutateAsync(payload);
            notify("Replay accepted");
          }}
          secondaryAction={(
            <>
              {item.status === "ACTIVE" ? <Button type="button" variant="secondary" onClick={() => withToast(() => mutations.deactivate.mutateAsync(), "Rule deactivated")}>Deactivate</Button> : <Button type="button" onClick={() => withToast(() => mutations.activate.mutateAsync(), "Rule activated")}>Activate</Button>}
              <Button type="button" variant="danger" onClick={() => withToast(async () => { await mutations.deleteRule.mutateAsync(); navigate("/app/automation"); }, "Rule deleted")}><Trash2 size={16} /> Delete</Button>
            </>
          )}
        />
      ) : <EmptyState icon={<Zap />} title="Rule not found" text="The selected automation rule could not be loaded." />}
    </>
  );
}
