import { Session } from "../auth/session";
import { ApiError } from "./errors";

export type Page<T> = {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
};

type RequestOptions = {
  method?: string;
  body?: unknown;
  tenant?: boolean;
  session?: Session | null;
  tenantSlug?: string;
};

const apiBaseUrl = import.meta.env.VITE_API_BASE_URL ?? "";

export async function apiRequest<T>(path: string, options: RequestOptions = {}): Promise<T> {
  const response = await fetch(`${apiBaseUrl}${path}`, {
    method: options.method ?? "GET",
    headers: {
      ...(options.body ? { "Content-Type": "application/json" } : {}),
      ...(options.session?.accessToken ? { Authorization: `Bearer ${options.session.accessToken}` } : {}),
      ...(options.tenant !== false && (options.tenantSlug ?? options.session?.tenantSlug)
        ? { "X-Tenant-Slug": options.tenantSlug ?? options.session?.tenantSlug ?? "" }
        : {}),
      "X-Correlation-Id": `web-${crypto.randomUUID()}`
    },
    body: options.body ? JSON.stringify(options.body) : undefined
  });

  if (response.status === 401) {
    window.dispatchEvent(new Event("insightflow:unauthorized"));
  }
  if (!response.ok) {
    throw new ApiError(`Request failed: ${response.status}`, response.status, await safeJson(response));
  }
  if (response.status === 204) {
    return undefined as T;
  }
  return (await response.json()) as T;
}

async function safeJson(response: Response) {
  try {
    return await response.json();
  } catch {
    return undefined;
  }
}
