import { ButtonHTMLAttributes } from "react";
import { cn } from "../../lib/utils";

type ButtonProps = ButtonHTMLAttributes<HTMLButtonElement> & {
  variant?: "primary" | "secondary" | "ghost" | "danger";
  size?: "sm" | "md" | "icon";
};

export function Button({ className, variant = "primary", size = "md", ...props }: ButtonProps) {
  return (
    <button
      className={cn(
        "inline-flex items-center justify-center gap-2 rounded-lg border text-sm font-semibold transition disabled:cursor-not-allowed disabled:opacity-55",
        size === "sm" && "h-9 px-3",
        size === "md" && "h-10 px-4",
        size === "icon" && "h-10 w-10 px-0",
        variant === "primary" && "border-graphite-900 bg-graphite-900 text-white hover:bg-graphite-800",
        variant === "secondary" && "border-slate-200 bg-white text-slate-800 hover:bg-slate-50",
        variant === "ghost" && "border-transparent bg-transparent text-slate-700 hover:bg-slate-100",
        variant === "danger" && "border-red-200 bg-red-50 text-red-700 hover:bg-red-100",
        className
      )}
      {...props}
    />
  );
}
