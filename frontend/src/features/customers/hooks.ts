import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useAuth } from "../../auth/AuthProvider";
import { customersApi, CustomerInput } from "./api";

export function useCustomers(query = "") {
  const { session } = useAuth();
  return useQuery({
    queryKey: ["customers", query, session?.tenantSlug],
    enabled: Boolean(session),
    queryFn: () => query ? customersApi.search(session!, query) : customersApi.list(session!)
  });
}

export function useCustomer(id?: string) {
  const { session } = useAuth();
  return useQuery({
    queryKey: ["customer", id, session?.tenantSlug],
    enabled: Boolean(session && id),
    queryFn: () => customersApi.detail(session!, id!)
  });
}

export function useCustomerMutations() {
  const { session } = useAuth();
  const queryClient = useQueryClient();
  const invalidate = () => queryClient.invalidateQueries({ queryKey: ["customers"] });
  return {
    create: useMutation({ mutationFn: (body: CustomerInput) => customersApi.create(session!, body), onSuccess: invalidate }),
    update: useMutation({ mutationFn: ({ id, body }: { id: string; body: CustomerInput }) => customersApi.update(session!, id, body), onSuccess: invalidate }),
    activate: useMutation({ mutationFn: (id: string) => customersApi.activate(session!, id), onSuccess: invalidate }),
    deactivate: useMutation({ mutationFn: (id: string) => customersApi.deactivate(session!, id), onSuccess: invalidate })
  };
}
