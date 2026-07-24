import { Session } from "../../auth/session";
import { apiRequest, Page } from "../../lib/api-client";

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

export type CustomerInput = {
  externalId?: string;
  email?: string;
  fullName?: string;
  plan?: string;
};

export const customersApi = {
  list: (session: Session, page = 0) => apiRequest<Page<Customer>>(`/api/v1/customers?page=${page}&size=20`, { session }),
  search: (session: Session, query: string) => apiRequest<Page<Customer>>(`/api/v1/customers/search?q=${encodeURIComponent(query)}&page=0&size=20`, { session }),
  detail: (session: Session, id: string) => apiRequest<Customer>(`/api/v1/customers/${id}`, { session }),
  create: (session: Session, body: CustomerInput) => apiRequest<Customer>("/api/v1/customers", { session, method: "POST", body }),
  update: (session: Session, id: string, body: CustomerInput) => apiRequest<Customer>(`/api/v1/customers/${id}`, { session, method: "PATCH", body }),
  deactivate: (session: Session, id: string) => apiRequest<Customer>(`/api/v1/customers/${id}/deactivate`, { session, method: "POST" }),
  activate: (session: Session, id: string) => apiRequest<Customer>(`/api/v1/customers/${id}/activate`, { session, method: "POST" })
};
