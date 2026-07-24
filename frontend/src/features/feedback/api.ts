import { Session } from "../../auth/session";
import { apiRequest, Page } from "../../lib/api-client";

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
  feedbackId: string;
  author: string;
  content: string;
  createdAt?: string;
};

export type FeedbackInput = {
  customerId?: string;
  source?: string;
  title?: string;
  content?: string;
  priority?: string;
  metadata?: Record<string, unknown>;
};

export type FeedbackFilters = {
  q?: string;
  status?: string;
  priority?: string;
  sentiment?: string;
  riskLevel?: string;
  category?: string;
  source?: string;
  customerId?: string;
  from?: string;
  to?: string;
};

export const feedbackApi = {
  list: (session: Session, filters: FeedbackFilters = {}) => {
    const hasSearch = Object.values(filters).some(Boolean);
    if (hasSearch) {
      const params = new URLSearchParams();
      Object.entries(filters).forEach(([key, value]) => value && params.set(key, value));
      params.set("page", "0");
      params.set("size", "20");
      return apiRequest<Page<Feedback>>(`/api/v1/feedbacks/search?${params}`, { session });
    }
    return apiRequest<Page<Feedback>>("/api/v1/feedbacks?page=0&size=20", { session });
  },
  detail: (session: Session, id: string) => apiRequest<Feedback>(`/api/v1/feedbacks/${id}`, { session }),
  create: (session: Session, body: FeedbackInput) => apiRequest<Feedback>("/api/v1/feedbacks", { session, method: "POST", body }),
  updateStatus: (session: Session, id: string, status: string) => apiRequest<Feedback>(`/api/v1/feedbacks/${id}/status`, { session, method: "PATCH", body: { status } }),
  updatePriority: (session: Session, id: string, priority: string) => apiRequest<Feedback>(`/api/v1/feedbacks/${id}/priority`, { session, method: "PATCH", body: { priority } }),
  assign: (session: Session, id: string, assignedTo: string) => apiRequest<Feedback>(`/api/v1/feedbacks/${id}/assignment`, { session, method: "PATCH", body: { assignedTo } }),
  archive: (session: Session, id: string) => apiRequest<Feedback>(`/api/v1/feedbacks/${id}/archive`, { session, method: "POST" }),
  restore: (session: Session, id: string) => apiRequest<Feedback>(`/api/v1/feedbacks/${id}/restore`, { session, method: "POST" }),
  notes: (session: Session, id: string) => apiRequest<FeedbackNote[]>(`/api/v1/feedbacks/${id}/notes`, { session }),
  addNote: (session: Session, id: string, content: string) => apiRequest<FeedbackNote>(`/api/v1/feedbacks/${id}/notes`, { session, method: "POST", body: { content } }),
  analyze: (session: Session, id: string) => apiRequest<void>(`/api/v1/feedbacks/${id}/ai-analysis`, { session, method: "POST" })
};
