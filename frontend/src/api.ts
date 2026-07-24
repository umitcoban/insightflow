export type Page<T> = {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
};

export type Session = {
  accessToken: string;
  username: string;
  role: "TENANT_ADMIN" | "SUPPORT_AGENT" | "PLATFORM_ADMIN";
  tenantSlug: string;
};

export type HealthResponse = {
  status: string;
  components?: Record<string, { status: string; details?: Record<string, unknown> }>;
};

export type Customer = {
  id: string;
  externalId: string;
  email: string;
  fullName: string;
  plan: string;
  status?: string;
  createdAt?: string;
  updatedAt?: string;
};

export type Feedback = {
  id: string;
  tenantId?: string;
  customerId?: string;
  source: string;
  title: string;
  content: string;
  status: string;
  priority: string;
  sentiment?: string;
  category?: string;
  riskLevel?: string;
  aiSummary?: string;
  suggestedAction?: string;
  assignedTo?: string;
  archivedAt?: string;
  createdAt?: string;
  updatedAt?: string;
};

export type FeedbackNote = {
  id: string;
  tenantId: string;
  feedbackId: string;
  author: string;
  content: string;
  createdAt?: string;
};

export type AutomationRule = {
  id: string;
  tenantId?: string;
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

export type AutomationDryRunResponse = {
  matched: boolean;
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

export type KnowledgeDocument = {
  id: string;
  title: string;
  source: string;
  createdAt?: string;
};

export type AssistantAnswer = {
  answer: string;
  sources?: Array<{
    documentId: string;
    chunkId: string;
    documentTitle: string;
    source: string;
    content: string;
    score: number;
  }>;
};

type RequestOptions = {
  method?: string;
  body?: unknown;
  tenant?: boolean;
  token?: string;
  tenantSlug?: string;
};

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? "";
const KEYCLOAK_BASE_URL = import.meta.env.VITE_KEYCLOAK_BASE_URL ?? "";

export const demoUsers = [
  { username: "acme-admin", password: "acme-admin", role: "TENANT_ADMIN" as const },
  { username: "acme-agent", password: "acme-agent", role: "SUPPORT_AGENT" as const },
  { username: "platform-admin", password: "platform-admin", role: "PLATFORM_ADMIN" as const }
];

export class ApiError extends Error {
  constructor(
    message: string,
    readonly status: number,
    readonly details?: unknown
  ) {
    super(message);
  }
}

export async function login(username: string, password: string, role: Session["role"], tenantSlug: string): Promise<Session> {
  const response = await fetch(`${KEYCLOAK_BASE_URL}/realms/insightflow/protocol/openid-connect/token`, {
    method: "POST",
    headers: { "Content-Type": "application/x-www-form-urlencoded" },
    body: new URLSearchParams({
      grant_type: "password",
      client_id: "insightflow-dev-client",
      username,
      password
    })
  });
  if (!response.ok) {
    throw new ApiError("Login failed", response.status, await safeJson(response));
  }
  const payload = (await response.json()) as { access_token: string };
  return { accessToken: payload.access_token, username, role, tenantSlug };
}

export async function request<T>(path: string, session: Session | null, options: RequestOptions = {}): Promise<T> {
  const response = await fetch(`${API_BASE_URL}${path}`, {
    method: options.method ?? "GET",
    headers: {
      ...(options.body ? { "Content-Type": "application/json" } : {}),
      ...(session?.accessToken ? { Authorization: `Bearer ${session.accessToken}` } : {}),
      ...(options.token ? { Authorization: `Bearer ${options.token}` } : {}),
      ...(options.tenant !== false && (options.tenantSlug ?? session?.tenantSlug)
        ? { "X-Tenant-Slug": options.tenantSlug ?? session?.tenantSlug ?? "" }
        : {}),
      "X-Correlation-Id": `web-${crypto.randomUUID()}`
    },
    body: options.body ? JSON.stringify(options.body) : undefined
  });
  if (!response.ok) {
    throw new ApiError(`Request failed: ${response.status}`, response.status, await safeJson(response));
  }
  if (response.status === 204) {
    return undefined as T;
  }
  return (await response.json()) as T;
}

export const api = {
  health: () => request<HealthResponse>("/actuator/health", null, { tenant: false }),
  readiness: () => request<HealthResponse>("/actuator/health/readiness", null, { tenant: false }),
  customers: (session: Session, page = 0) => request<Page<Customer>>(`/api/v1/customers?page=${page}&size=8`, session),
  createCustomer: (session: Session, body: Partial<Customer>) => request<Customer>("/api/v1/customers", session, { method: "POST", body }),
  feedbacks: (session: Session, query = "") => request<Page<Feedback>>(`/api/v1/feedbacks${query || "?page=0&size=12"}`, session),
  searchFeedbacks: (session: Session, q: string) =>
    request<Page<Feedback>>(`/api/v1/feedbacks/search?q=${encodeURIComponent(q)}&page=0&size=12`, session),
  feedback: (session: Session, id: string) => request<Feedback>(`/api/v1/feedbacks/${id}`, session),
  createFeedback: (session: Session, body: Record<string, unknown>) =>
    request<Feedback>("/api/v1/feedbacks", session, { method: "POST", body }),
  updateFeedbackStatus: (session: Session, id: string, status: string) =>
    request<Feedback>(`/api/v1/feedbacks/${id}/status`, session, { method: "PATCH", body: { status } }),
  updateFeedbackPriority: (session: Session, id: string, priority: string) =>
    request<Feedback>(`/api/v1/feedbacks/${id}/priority`, session, { method: "PATCH", body: { priority } }),
  assignFeedback: (session: Session, id: string, assignedTo: string) =>
    request<Feedback>(`/api/v1/feedbacks/${id}/assignment`, session, { method: "PATCH", body: { assignedTo } }),
  archiveFeedback: (session: Session, id: string) =>
    request<Feedback>(`/api/v1/feedbacks/${id}/archive`, session, { method: "POST" }),
  restoreFeedback: (session: Session, id: string) =>
    request<Feedback>(`/api/v1/feedbacks/${id}/restore`, session, { method: "POST" }),
  feedbackNotes: (session: Session, id: string) => request<FeedbackNote[]>(`/api/v1/feedbacks/${id}/notes`, session),
  addFeedbackNote: (session: Session, id: string, content: string) =>
    request<FeedbackNote>(`/api/v1/feedbacks/${id}/notes`, session, { method: "POST", body: { content } }),
  analyzeFeedback: (session: Session, id: string) =>
    request<void>(`/api/v1/feedbacks/${id}/ai-analysis`, session, { method: "POST" }),
  automationRules: (session: Session) => request<Page<AutomationRule>>("/api/v1/automation/rules?page=0&size=12", session),
  createAutomationRule: (session: Session, body: Record<string, unknown>) =>
    request<AutomationRule>("/api/v1/automation/rules", session, { method: "POST", body }),
  updateAutomationRule: (session: Session, id: string, body: Record<string, unknown>) =>
    request<AutomationRule>(`/api/v1/automation/rules/${id}`, session, { method: "PATCH", body }),
  activateAutomationRule: (session: Session, id: string) =>
    request<AutomationRule>(`/api/v1/automation/rules/${id}/activate`, session, { method: "POST" }),
  deactivateAutomationRule: (session: Session, id: string) =>
    request<AutomationRule>(`/api/v1/automation/rules/${id}/deactivate`, session, { method: "POST" }),
  deleteAutomationRule: (session: Session, id: string) =>
    request<void>(`/api/v1/automation/rules/${id}`, session, { method: "DELETE" }),
  dryRunAutomationRule: (session: Session, id: string, payload: Record<string, unknown>) =>
    request<AutomationDryRunResponse>(`/api/v1/automation/rules/${id}/dry-run`, session, { method: "POST", body: { payload } }),
  replayAutomationRule: (session: Session, id: string, payload: Record<string, unknown>) =>
    request<void>(`/api/v1/automation/rules/${id}/replay`, session, { method: "POST", body: { payload } }),
  automationExecutions: (session: Session) =>
    request<Page<AutomationExecution>>("/api/v1/automation/executions?page=0&size=12", session),
  knowledgeDocuments: (session: Session) => request<Page<KnowledgeDocument>>("/api/v1/knowledge/documents?page=0&size=8", session),
  createKnowledgeDocument: (session: Session, body: Record<string, unknown>) =>
    request<KnowledgeDocument>("/api/v1/knowledge/documents", session, { method: "POST", body }),
  deleteKnowledgeDocument: (session: Session, id: string) =>
    request<void>(`/api/v1/knowledge/documents/${id}`, session, { method: "DELETE" }),
  askAssistant: (session: Session, question: string) =>
    request<AssistantAnswer>("/api/v1/assistant/questions", session, { method: "POST", body: { question } })
};

async function safeJson(response: Response): Promise<unknown> {
  try {
    return await response.json();
  } catch {
    return undefined;
  }
}
