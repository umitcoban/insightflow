import * as ToastPrimitive from "@radix-ui/react-toast";
import { createContext, ReactNode, useContext, useMemo, useState } from "react";

type ToastMessage = { id: string; title: string; description?: string };
type ToastContextValue = { notify: (title: string, description?: string) => void };
const ToastContext = createContext<ToastContextValue | null>(null);

export function ToastProvider({ children }: { children: ReactNode }) {
  const [messages, setMessages] = useState<ToastMessage[]>([]);
  const value = useMemo(() => ({
    notify(title: string, description?: string) {
      const id = crypto.randomUUID();
      setMessages((current) => [...current, { id, title, description }]);
      window.setTimeout(() => setMessages((current) => current.filter((item) => item.id !== id)), 4500);
    }
  }), []);

  return (
    <ToastContext.Provider value={value}>
      <ToastPrimitive.Provider swipeDirection="right">
        {children}
        {messages.map((message) => (
          <ToastPrimitive.Root key={message.id} className="rounded-lg border border-slate-200 bg-white p-4 shadow-lg">
            <ToastPrimitive.Title className="text-sm font-semibold text-slate-950">{message.title}</ToastPrimitive.Title>
            {message.description ? <ToastPrimitive.Description className="mt-1 text-sm text-slate-600">{message.description}</ToastPrimitive.Description> : null}
          </ToastPrimitive.Root>
        ))}
        <ToastPrimitive.Viewport className="fixed bottom-4 right-4 z-50 grid w-96 max-w-[calc(100vw-2rem)] gap-2" />
      </ToastPrimitive.Provider>
    </ToastContext.Provider>
  );
}

export function useToast() {
  const value = useContext(ToastContext);
  if (!value) {
    throw new Error("useToast must be used inside ToastProvider");
  }
  return value;
}
