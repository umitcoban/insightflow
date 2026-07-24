import { Session } from "../../auth/session";
import { apiRequest, Page } from "../../lib/api-client";

export type AutomationRule = {
  id: string;
  name: string;
  description?: string;
  triggerEventType: string;
  conditionJson: Record<string, unknown>;
  actionJson: Array<Record<string, unknown>>;
  status: string;
  priority: number;
  createdAt?: string;
  updatedAt?: string;
};

export type AutomationExecution = {
  id: string;
  ruleId: string;
  sourceEventType: string;
  matched: boolean;
  status: string;
  startedAt?: string;
  finishedAt?: string;
  errorMessage?: string;
};

export type AutomationActionExecution = {
  id: string;
  actionType: string;
  status: string;
  requestPayload?: Record<string, unknown>;
  resultPayload?: Record<string, unknown>;
  errorMessage?: string;
  attemptCount: number;
};

export type AutomationRuleInput = {
  name?: string;
  description?: string;
  triggerEventType?: string;
  conditionJson?: Record<string, unknown>;
  actionJson?: Array<Record<string, unknown>>;
  priority?: number;
};

export const automationApi = {
  listRules: (session: Session) => apiRequest<Page<AutomationRule>>("/api/v1/automation/rules?page=0&size=20", { session }),
  detail: (session: Session, id: string) => apiRequest<AutomationRule>(`/api/v1/automation/rules/${id}`, { session }),
  create: (session: Session, body: AutomationRuleInput) => apiRequest<AutomationRule>("/api/v1/automation/rules", { session, method: "POST", body }),
  update: (session: Session, id: string, body: AutomationRuleInput) => apiRequest<AutomationRule>(`/api/v1/automation/rules/${id}`, { session, method: "PATCH", body }),
  activate: (session: Session, id: string) => apiRequest<AutomationRule>(`/api/v1/automation/rules/${id}/activate`, { session, method: "POST" }),
  deactivate: (session: Session, id: string) => apiRequest<AutomationRule>(`/api/v1/automation/rules/${id}/deactivate`, { session, method: "POST" }),
  delete: (session: Session, id: string) => apiRequest<void>(`/api/v1/automation/rules/${id}`, { session, method: "DELETE" }),
  dryRun: (session: Session, id: string, payload: Record<string, unknown>) => apiRequest<{ matched: boolean }>(`/api/v1/automation/rules/${id}/dry-run`, { session, method: "POST", body: { payload } }),
  replay: (session: Session, id: string, payload: Record<string, unknown>) => apiRequest<void>(`/api/v1/automation/rules/${id}/replay`, { session, method: "POST", body: { payload } }),
  executions: (session: Session) => apiRequest<Page<AutomationExecution>>("/api/v1/automation/executions?page=0&size=20", { session }),
  actionExecutions: (session: Session, executionId: string) => apiRequest<AutomationActionExecution[]>(`/api/v1/automation/executions/${executionId}/actions`, { session })
};
