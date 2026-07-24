import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useAuth } from "../../auth/AuthProvider";
import { tenantApi } from "./api";

export function useTenant() {
  const { session } = useAuth();
  return useQuery({
    queryKey: ["tenant", session?.tenantSlug],
    enabled: Boolean(session),
    queryFn: () => tenantApi.current(session!)
  });
}

export function useTenantSettings() {
  const { session } = useAuth();
  return useQuery({
    queryKey: ["tenant-settings", session?.tenantSlug],
    enabled: Boolean(session),
    queryFn: () => tenantApi.settings(session!)
  });
}

export function useTenantMutations() {
  const { session } = useAuth();
  const queryClient = useQueryClient();
  const invalidate = () => {
    queryClient.invalidateQueries({ queryKey: ["tenant"] });
    queryClient.invalidateQueries({ queryKey: ["tenant-settings"] });
  };
  return {
    updateSettings: useMutation({ mutationFn: (settings: Record<string, unknown>) => tenantApi.updateSettings(session!, settings), onSuccess: invalidate }),
    suspend: useMutation({ mutationFn: () => tenantApi.suspend(session!), onSuccess: invalidate }),
    reactivate: useMutation({ mutationFn: () => tenantApi.reactivate(session!), onSuccess: invalidate })
  };
}
