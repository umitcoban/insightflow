import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useAuth } from "../../auth/AuthProvider";
import { feedbackApi, FeedbackFilters, FeedbackInput } from "./api";

export function useFeedbacks(filters: FeedbackFilters) {
  const { session } = useAuth();
  return useQuery({
    queryKey: ["feedbacks", filters, session?.tenantSlug],
    enabled: Boolean(session),
    queryFn: () => feedbackApi.list(session!, filters)
  });
}

export function useFeedback(id?: string) {
  const { session } = useAuth();
  return useQuery({
    queryKey: ["feedback", id, session?.tenantSlug],
    enabled: Boolean(session && id),
    queryFn: () => feedbackApi.detail(session!, id!)
  });
}

export function useFeedbackNotes(id?: string) {
  const { session } = useAuth();
  return useQuery({
    queryKey: ["feedback-notes", id, session?.tenantSlug],
    enabled: Boolean(session && id),
    queryFn: () => feedbackApi.notes(session!, id!)
  });
}

export function useFeedbackMutations(id?: string) {
  const { session } = useAuth();
  const queryClient = useQueryClient();
  const invalidate = () => {
    queryClient.invalidateQueries({ queryKey: ["feedbacks"] });
    if (id) {
      queryClient.invalidateQueries({ queryKey: ["feedback", id] });
      queryClient.invalidateQueries({ queryKey: ["feedback-notes", id] });
    }
  };
  return {
    create: useMutation({ mutationFn: (body: FeedbackInput) => feedbackApi.create(session!, body), onSuccess: invalidate }),
    status: useMutation({ mutationFn: (status: string) => feedbackApi.updateStatus(session!, id!, status), onSuccess: invalidate }),
    priority: useMutation({ mutationFn: (priority: string) => feedbackApi.updatePriority(session!, id!, priority), onSuccess: invalidate }),
    assign: useMutation({ mutationFn: (assignedTo: string) => feedbackApi.assign(session!, id!, assignedTo), onSuccess: invalidate }),
    archive: useMutation({ mutationFn: () => feedbackApi.archive(session!, id!), onSuccess: invalidate }),
    restore: useMutation({ mutationFn: () => feedbackApi.restore(session!, id!), onSuccess: invalidate }),
    analyze: useMutation({ mutationFn: () => feedbackApi.analyze(session!, id!), onSuccess: invalidate }),
    note: useMutation({ mutationFn: (content: string) => feedbackApi.addNote(session!, id!, content), onSuccess: invalidate })
  };
}
