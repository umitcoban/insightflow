import { ArrowRight, Bot, Gauge, MessageSquareText, ShieldCheck, Zap } from "lucide-react";
import { Link } from "react-router";
import { Button } from "../../components/ui/button";
import { Badge } from "../../components/ui/badge";

export function LandingPage() {
  return (
    <main className="min-h-screen bg-[#f7faf9] text-slate-950">
      <header className="mx-auto flex max-w-7xl items-center justify-between px-5 py-5">
        <div className="flex items-center gap-3">
          <div className="grid h-10 w-10 place-items-center rounded-lg bg-teal-700 text-sm font-bold text-white">IF</div>
          <strong>InsightFlow</strong>
        </div>
        <Link to="/login"><Button variant="secondary">Sign in</Button></Link>
      </header>
      <section className="mx-auto grid max-w-7xl gap-10 px-5 py-14 lg:grid-cols-[0.92fr_1.08fr] lg:items-center">
        <div>
          <Badge className="bg-teal-50 text-teal-800">Customer intelligence platform</Badge>
          <h1 className="mt-5 text-5xl font-semibold leading-tight tracking-tight">Turn feedback into accountable product operations.</h1>
          <p className="mt-5 max-w-xl text-base leading-7 text-slate-600">
            InsightFlow brings feedback intake, tenant knowledge, AI enrichment, and automation workflows into one calm operational console.
          </p>
          <div className="mt-7 flex flex-wrap gap-3">
            <Link to="/login"><Button>Open workspace <ArrowRight size={16} /></Button></Link>
            <a href="#capabilities"><Button variant="secondary">Explore capabilities</Button></a>
          </div>
        </div>
        <div className="rounded-lg border border-slate-200 bg-white p-4 shadow-xl">
          <div className="grid gap-3 rounded-lg bg-slate-950 p-4 text-white">
            <div className="flex items-center justify-between border-b border-white/10 pb-3">
              <span className="text-sm font-semibold">Operational Snapshot</span>
              <span className="rounded-full bg-emerald-400/15 px-2 py-1 text-xs text-emerald-200">READY</span>
            </div>
            <div className="grid grid-cols-3 gap-3">
              {["43 Open", "11 Critical", "86% AI enriched"].map((item) => <div key={item} className="rounded-lg bg-white/7 p-3 text-sm">{item}</div>)}
            </div>
            <div className="grid gap-2">
              {["Payment failed after renewal", "Refund request from App Store", "Webhook replay completed"].map((item) => (
                <div key={item} className="flex items-center justify-between rounded-lg bg-white/7 p-3 text-sm">
                  <span>{item}</span>
                  <span className="text-teal-200">tracked</span>
                </div>
              ))}
            </div>
          </div>
        </div>
      </section>
      <section id="capabilities" className="mx-auto grid max-w-7xl gap-4 px-5 pb-16 md:grid-cols-5">
        {[
          [MessageSquareText, "Feedback intelligence"],
          [Bot, "Tenant assistant"],
          [Zap, "Automation playground"],
          [Gauge, "Operational health"],
          [ShieldCheck, "Tenant boundaries"]
        ].map(([Icon, title]) => {
          const Component = Icon as typeof MessageSquareText;
          return (
            <div key={String(title)} className="rounded-lg border border-slate-200 bg-white p-4">
              <Component className="text-teal-700" size={22} />
              <strong className="mt-4 block text-sm">{String(title)}</strong>
            </div>
          );
        })}
      </section>
    </main>
  );
}
