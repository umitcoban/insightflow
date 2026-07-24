import { useQuery } from "@tanstack/react-query";
import { operationsApi } from "./api";

export function useHealth() {
  return useQuery({ queryKey: ["health"], queryFn: operationsApi.health, refetchInterval: 30_000 });
}

export function useReadiness() {
  return useQuery({ queryKey: ["readiness"], queryFn: operationsApi.readiness, refetchInterval: 30_000 });
}
