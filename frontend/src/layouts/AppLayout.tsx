import { Activity, Bot, Gauge, LogOut, MessageSquareText, Settings, Users, Zap, FileText } from "lucide-react";
import { NavLink, Outlet, useNavigate } from "react-router";
import { useAuth } from "../auth/AuthProvider";
import { Button } from "../components/ui/button";
import { cn } from "../lib/utils";

const navItems = [
  { to: "/app/dashboard", label: "Dashboard", icon: Gauge },
  { to: "/app/feedback", label: "Feedback", icon: MessageSquareText },
  { to: "/app/customers", label: "Customers", icon: Users },
  { to: "/app/assistant", label: "Assistant", icon: Bot },
  { to: "/app/knowledge", label: "Knowledge", icon: FileText },
  { to: "/app/automation", label: "Automation", icon: Zap },
  { to: "/app/operations", label: "Operations", icon: Activity },
  { to: "/app/settings/tenant", label: "Settings", icon: Settings }
];

export function AppLayout() {
  const { session, signOut } = useAuth();
  const navigate = useNavigate();

  return (
    <div className="min-h-screen bg-slate-50 text-slate-950 lg:grid lg:grid-cols-[272px_minmax(0,1fr)]">
      <aside className="border-b border-slate-200 bg-white lg:sticky lg:top-0 lg:h-screen lg:border-b-0 lg:border-r">
        <div className="flex h-full flex-col gap-6 p-5">
          <div className="flex items-center gap-3">
            <div className="grid h-10 w-10 place-items-center rounded-lg bg-teal-700 text-white">IF</div>
            <div>
              <strong className="block text-sm">InsightFlow</strong>
              <span className="text-xs text-slate-500">Product console</span>
            </div>
          </div>
          <nav className="grid gap-1">
            {navItems.map((item) => {
              const Icon = item.icon;
              return (
                <NavLink
                  key={item.to}
                  to={item.to}
                  className={({ isActive }) => cn(
                    "flex h-10 items-center gap-3 rounded-lg px-3 text-sm font-semibold text-slate-600 hover:bg-slate-100 hover:text-slate-950",
                    isActive && "bg-teal-50 text-teal-800"
                  )}
                >
                  <Icon size={18} />
                  {item.label}
                </NavLink>
              );
            })}
          </nav>
          <div className="mt-auto rounded-lg border border-slate-200 bg-slate-50 p-3 text-sm">
            <span className="text-xs font-semibold uppercase text-slate-500">Tenant</span>
            <strong className="mt-1 block">{session?.tenantSlug}</strong>
            <span className="text-xs text-slate-500">{session?.username} · {session?.role}</span>
          </div>
        </div>
      </aside>
      <main className="min-w-0">
        <header className="sticky top-0 z-20 flex items-center justify-between border-b border-slate-200 bg-white/90 px-5 py-3 backdrop-blur">
          <div className="text-sm text-slate-500">Tenant-scoped operational workspace</div>
          <Button variant="secondary" size="sm" onClick={() => {
            signOut();
            navigate("/login");
          }}>
            <LogOut size={16} /> Sign out
          </Button>
        </header>
        <div className="mx-auto grid max-w-7xl gap-6 p-5 lg:p-8">
          <Outlet />
        </div>
      </main>
    </div>
  );
}
