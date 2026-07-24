import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useAuth } from "../../auth/AuthProvider";
import { automationApi, AutomationRuleInput } from "./api";

export function useAutomationRules() {
  const { session } = useAuth();
  return useQuery({
    queryKey: ["automation-rules", session?.tenantSlug],
    enabled: Boolean(session),
    queryFn: () => automationApi.listRules(session!)
  });
}

export function useAutomationRule(id?: string) {
  const { session } = useAuth();
  return useQuery({
    queryKey: ["automation-rule", id, session?.tenantSlug],
    enabled: Boolean(session && id),
    queryFn: () => automationApi.detail(session!, id!)
  });
}

export function useAutomationExecutions() {
  const { session } = useAuth();
  return useQuery({
    queryKey: ["automation-executions", session?.tenantSlug],
    enabled: Boolean(session),
    queryFn: () => automationApi.executions(session!)
  });
}

export function useAutomationActionExecutions(executionId?: string) {
  const { session } = useAuth();
  return useQuery({
    queryKey: ["automation-action-executions", executionId, session?.tenantSlug],
    enabled: Boolean(session && executionId),
    queryFn: () => automationApi.actionExecutions(session!, executionId!)
  });
}

export function useAutomationMutations(id?: string) {
  const { session } = useAuth();
  const queryClient = useQueryClient();
  const invalidate = () => {
    queryClient.invalidateQueries({ queryKey: ["automation-rules"] });
    queryClient.invalidateQueries({ queryKey: ["automation-executions"] });
    if (id) {
      queryClient.invalidateQueries({ queryKey: ["automation-rule", id] });
    }
  };
  return {
    create: useMutation({ mutationFn: (body: AutomationRuleInput) => automationApi.create(session!, body), onSuccess: invalidate }),
    update: useMutation({ mutationFn: (body: AutomationRuleInput) => automationApi.update(session!, id!, body), onSuccess: invalidate }),
    activate: useMutation({ mutationFn: () => automationApi.activate(session!, id!), onSuccess: invalidate }),
    deactivate: useMutation({ mutationFn: () => automationApi.deactivate(session!, id!), onSuccess: invalidate }),
    deleteRule: useMutation({ mutationFn: () => automationApi.delete(session!, id!), onSuccess: invalidate }),
    dryRun: useMutation({ mutationFn: (payload: Record<string, unknown>) => automationApi.dryRun(session!, id!, payload) }),
    replay: useMutation({ mutationFn: (payload: Record<string, unknown>) => automationApi.replay(session!, id!, payload), onSuccess: invalidate })
  };
}
