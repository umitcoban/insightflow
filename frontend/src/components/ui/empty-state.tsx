import { ReactNode } from "react";

export function EmptyState({ icon, title, text }: { icon: ReactNode; title: string; text: string }) {
  return (
    <div className="grid min-h-48 place-items-center rounded-lg border border-dashed border-slate-200 bg-slate-50 p-6 text-center">
      <div className="grid max-w-sm place-items-center gap-2 text-slate-500">
        <div className="text-teal-700">{icon}</div>
        <strong className="text-slate-900">{title}</strong>
        <span className="text-sm leading-6">{text}</span>
      </div>
    </div>
  );
}
