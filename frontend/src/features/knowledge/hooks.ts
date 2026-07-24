import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useAuth } from "../../auth/AuthProvider";
import { knowledgeApi } from "./api";

export function useKnowledgeDocuments() {
  const { session } = useAuth();
  return useQuery({
    queryKey: ["knowledge-documents", session?.tenantSlug],
    enabled: Boolean(session),
    queryFn: () => knowledgeApi.list(session!)
  });
}

export function useKnowledgeMutations() {
  const { session } = useAuth();
  const queryClient = useQueryClient();
  const invalidate = () => queryClient.invalidateQueries({ queryKey: ["knowledge-documents"] });
  return {
    create: useMutation({ mutationFn: (body: { title?: string; source?: string; content?: string }) => knowledgeApi.create(session!, body), onSuccess: invalidate }),
    deleteDocument: useMutation({ mutationFn: (id: string) => knowledgeApi.delete(session!, id), onSuccess: invalidate }),
    ask: useMutation({ mutationFn: (question: string) => knowledgeApi.ask(session!, question) })
  };
}
