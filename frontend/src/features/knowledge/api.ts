import { Session } from "../../auth/session";
import { apiRequest, Page } from "../../lib/api-client";

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

export const knowledgeApi = {
  list: (session: Session) => apiRequest<Page<KnowledgeDocument>>("/api/v1/knowledge/documents?page=0&size=20", { session }),
  create: (session: Session, body: { title?: string; source?: string; content?: string }) => apiRequest<KnowledgeDocument>("/api/v1/knowledge/documents", { session, method: "POST", body }),
  delete: (session: Session, id: string) => apiRequest<void>(`/api/v1/knowledge/documents/${id}`, { session, method: "DELETE" }),
  ask: (session: Session, question: string) => apiRequest<AssistantAnswer>("/api/v1/assistant/questions", { session, method: "POST", body: { question } })
};
