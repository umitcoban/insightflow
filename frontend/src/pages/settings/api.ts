import { Session } from "../../auth/session";
import { apiRequest } from "../../lib/api-client";

export type Tenant = {
  id: string;
  slug: string;
  name: string;
  status: string;
  createdAt?: string;
  updatedAt?: string;
};

export type TenantSettings = {
  tenantId: string;
  settings: Record<string, unknown>;
  updatedAt?: string;
};

export const tenantApi = {
  current: (session: Session) => apiRequest<Tenant>(`/api/v1/tenants/${session.tenantSlug}`, { session }),
  settings: (session: Session) => apiRequest<TenantSettings>(`/api/v1/tenants/${session.tenantSlug}/settings`, { session }),
  updateSettings: (session: Session, settings: Record<string, unknown>) => apiRequest<TenantSettings>(`/api/v1/tenants/${session.tenantSlug}/settings`, { session, method: "PUT", body: { settings } }),
  suspend: (session: Session) => apiRequest<Tenant>(`/api/v1/tenants/${session.tenantSlug}/suspend`, { session, method: "POST" }),
  reactivate: (session: Session) => apiRequest<Tenant>(`/api/v1/tenants/${session.tenantSlug}/reactivate`, { session, method: "POST" })
};
