export function StatusDot({ status }: { status?: string }) {
  return <span className={status === "UP" ? "h-2.5 w-2.5 rounded-full bg-emerald-600 shadow-[0_0_0_4px_#dcfce7]" : "h-2.5 w-2.5 rounded-full bg-red-600 shadow-[0_0_0_4px_#fee2e2]"} />;
}
