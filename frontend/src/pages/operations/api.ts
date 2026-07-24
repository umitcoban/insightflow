import { apiRequest } from "../../lib/api-client";

export type HealthResponse = {
  status: string;
  components?: Record<string, { status: string; details?: Record<string, unknown> }>;
};

export const operationsApi = {
  health: () => apiRequest<HealthResponse>("/actuator/health", { tenant: false }),
  readiness: () => apiRequest<HealthResponse>("/actuator/health/readiness", { tenant: false })
};
