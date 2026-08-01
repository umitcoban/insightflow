import {
  ArrowRight,
  Bot,
  Braces,
  CheckCircle2,
  Gauge,
  GitBranch,
  MessageSquareText,
  RadioTower,
  ShieldCheck,
  Sparkles,
  Workflow,
  Zap
} from "lucide-react";
import { Link } from "react-router";
import { Badge } from "../../components/ui/badge";
import { Button } from "../../components/ui/button";

const capabilities = [
  {
    icon: MessageSquareText,
    title: "Feedback intelligence",
    text: "Collect product feedback, enrich it with AI context and keep every customer signal traceable."
  },
  {
    icon: Bot,
    title: "Tenant knowledge assistant",
    text: "Answer support and product questions from tenant-scoped knowledge documents with citations."
  },
  {
    icon: Workflow,
    title: "Readable automation",
    text: "Create When, If and Then workflows without making operators edit raw JSON first."
  },
  {
    icon: Gauge,
    title: "Operational readiness",
    text: "Watch API, Ollama, Elasticsearch, Kafka and PostgreSQL status from one console."
  }
];

const workflowSteps = [
  ["When", "AI analysis completed"],
  ["If", "sentiment is NEGATIVE and risk is HIGH"],
  ["Then", "log action and notify webhook"]
];

export function LandingPage() {
  return (
    <main className="min-h-screen bg-[#f5f8f7] text-slate-950">
      <header className="fixed inset-x-0 top-0 z-30 border-b border-white/50 bg-white/80 backdrop-blur">
        <div className="mx-auto flex max-w-7xl items-center justify-between px-5 py-4">
          <Link to="/" className="flex items-center gap-3">
            <div className="grid h-10 w-10 place-items-center rounded-lg bg-teal-700 text-sm font-bold text-white">IF</div>
            <div>
              <strong className="block leading-5">InsightFlow</strong>
              <span className="text-xs text-slate-500">Customer operations intelligence</span>
            </div>
          </Link>
          <nav className="landing-nav text-sm font-semibold text-slate-600">
            <a href="#platform">Platform</a>
            <a href="#workflow">Workflow</a>
            <a href="#readiness">Readiness</a>
          </nav>
          <Link to="/login"><Button variant="secondary">Sign in</Button></Link>
        </div>
      </header>

      <section className="relative isolate min-h-[760px] overflow-hidden pt-24">
        <div className="absolute inset-0 bg-[linear-gradient(90deg,rgba(245,248,247,0.98)_0%,rgba(245,248,247,0.88)_44%,rgba(245,248,247,0.18)_100%)]" />
        <div className="absolute inset-y-0 right-0 -z-10 w-full lg:w-[70%]">
          <HeroConsole />
        </div>
        <div className="relative mx-auto grid min-h-[660px] max-w-7xl content-center px-5 py-12">
          <div className="max-w-2xl">
            <Badge className="bg-teal-50 text-teal-800">Production-ready backend workspace</Badge>
            <h1 className="mt-5 text-5xl font-semibold leading-[1.02] tracking-tight text-slate-950 md:text-6xl">
              InsightFlow
            </h1>
            <p className="mt-5 max-w-xl text-lg leading-8 text-slate-600">
              Customer feedback, AI enrichment, tenant knowledge and automation workflows in one focused product operations console.
            </p>
            <div className="mt-8 flex flex-wrap gap-3">
              <Link to="/login"><Button>Open workspace <ArrowRight size={16} /></Button></Link>
              <a href="#platform"><Button variant="secondary">View platform</Button></a>
            </div>
            <div className="mt-10 grid max-w-xl gap-3 sm:grid-cols-3">
              <Metric value="86%" label="AI enriched" />
              <Metric value="11" label="critical signals" />
              <Metric value="5" label="ready services" />
            </div>
          </div>
        </div>
      </section>

      <section id="platform" className="border-y border-slate-200 bg-white">
        <div className="mx-auto grid max-w-7xl gap-8 px-5 py-16 lg:grid-cols-[0.78fr_1.22fr] lg:items-start">
          <div>
            <p className="text-xs font-bold uppercase text-teal-700">Platform surface</p>
            <h2 className="mt-3 text-3xl font-semibold tracking-tight">Built around the daily product operations loop.</h2>
            <p className="mt-4 text-sm leading-6 text-slate-600">
              The home page now mirrors what the product actually does: intake, understand, search, answer, automate and operate.
            </p>
          </div>
          <div className="grid gap-4 md:grid-cols-2">
            {capabilities.map((item) => {
              const Icon = item.icon;
              return (
                <article key={item.title} className="rounded-lg border border-slate-200 bg-[#fbfcfc] p-5">
                  <Icon className="text-teal-700" size={22} />
                  <h3 className="mt-4 text-base font-semibold">{item.title}</h3>
                  <p className="mt-2 text-sm leading-6 text-slate-600">{item.text}</p>
                </article>
              );
            })}
          </div>
        </div>
      </section>

      <section id="workflow" className="bg-[#eef4f2]">
        <div className="mx-auto grid max-w-7xl gap-8 px-5 py-16 lg:grid-cols-[1fr_1fr] lg:items-center">
          <div>
            <p className="text-xs font-bold uppercase text-blue-700">Automation without guesswork</p>
            <h2 className="mt-3 text-3xl font-semibold tracking-tight">Rules read like a decision your team would actually say out loud.</h2>
            <p className="mt-4 text-sm leading-6 text-slate-600">
              Instead of asking users to understand condition DSL first, the interface now starts from a readable workflow sentence and generates the backend payload.
            </p>
          </div>
          <div className="grid gap-3">
            {workflowSteps.map(([label, text], index) => (
              <div key={label} className="grid grid-cols-[76px_1fr] items-center gap-4 rounded-lg border border-slate-200 bg-white p-4">
                <span className="rounded-lg bg-slate-950 px-3 py-2 text-center text-sm font-bold text-white">{label}</span>
                <div className="flex items-center justify-between gap-3">
                  <strong className="text-sm">{text}</strong>
                  {index < workflowSteps.length - 1 ? <ArrowRight className="hidden text-slate-400 sm:block" size={18} /> : <CheckCircle2 className="text-teal-700" size={18} />}
                </div>
              </div>
            ))}
          </div>
        </div>
      </section>

      <section id="readiness" className="bg-slate-950 text-white">
        <div className="mx-auto grid max-w-7xl gap-8 px-5 py-16 lg:grid-cols-[0.95fr_1.05fr] lg:items-center">
          <div>
            <p className="text-xs font-bold uppercase text-teal-300">Operational confidence</p>
            <h2 className="mt-3 text-3xl font-semibold tracking-tight">Designed for the messy middle between demo and production.</h2>
            <p className="mt-4 text-sm leading-6 text-slate-300">
              InsightFlow exposes tenant boundaries, model readiness, search availability and event processing status so failures become visible.
            </p>
          </div>
          <div className="grid gap-3 sm:grid-cols-2">
            {[
              [RadioTower, "Kafka domain events", "streaming"],
              [Sparkles, "Ollama enrichment", "model ready"],
              [Braces, "Elasticsearch vectors", "indexed"],
              [ShieldCheck, "Tenant isolation", "enforced"]
            ].map(([Icon, title, state]) => {
              const Component = Icon as typeof RadioTower;
              return (
                <div key={String(title)} className="rounded-lg border border-white/10 bg-white/5 p-4">
                  <div className="flex items-center justify-between gap-3">
                    <Component className="text-teal-300" size={20} />
                    <span className="rounded-full bg-emerald-400/10 px-2 py-1 text-xs font-bold text-emerald-200">{String(state)}</span>
                  </div>
                  <strong className="mt-5 block text-sm">{String(title)}</strong>
                </div>
              );
            })}
          </div>
        </div>
      </section>
    </main>
  );
}

function Metric({ value, label }: { value: string; label: string }) {
  return (
    <div className="rounded-lg border border-slate-200 bg-white/80 p-4 shadow-sm backdrop-blur">
      <strong className="block text-2xl text-slate-950">{value}</strong>
      <span className="mt-1 block text-xs font-semibold uppercase text-slate-500">{label}</span>
    </div>
  );
}

function HeroConsole() {
  return (
    <div className="absolute inset-0 hidden lg:block">
      <div className="absolute left-[16%] top-[8%] h-[620px] w-[940px] rotate-[-2deg] rounded-lg border border-slate-300 bg-slate-950 p-4 shadow-2xl">
        <div className="flex items-center justify-between border-b border-white/10 pb-3">
          <div className="flex items-center gap-2">
            <span className="h-2.5 w-2.5 rounded-full bg-red-300" />
            <span className="h-2.5 w-2.5 rounded-full bg-amber-300" />
            <span className="h-2.5 w-2.5 rounded-full bg-emerald-300" />
          </div>
          <span className="text-xs font-bold uppercase text-slate-400">Tenant: acme</span>
        </div>
        <div className="grid grid-cols-[210px_1fr] gap-4 pt-4">
          <div className="grid content-start gap-2">
            {["Dashboard", "Feedback", "Assistant", "Knowledge", "Automation", "Operations"].map((item, index) => (
              <div key={item} className={`rounded-lg px-3 py-3 text-sm font-semibold ${index === 4 ? "bg-teal-400/15 text-teal-100" : "bg-white/5 text-slate-300"}`}>{item}</div>
            ))}
          </div>
          <div className="grid gap-4">
            <div className="grid grid-cols-3 gap-3">
              {["43 open", "11 critical", "86% enriched"].map((item) => <div key={item} className="rounded-lg bg-white/[0.08] p-4 text-sm font-semibold text-white">{item}</div>)}
            </div>
            <div className="grid gap-3 rounded-lg bg-white p-4 text-slate-950">
              <div className="flex items-center justify-between">
                <strong>Readable workflow</strong>
                <Badge className="bg-amber-50 text-amber-800">ACTIVE</Badge>
              </div>
              {workflowSteps.map(([label, text]) => (
                <div key={label} className="grid grid-cols-[72px_1fr] items-center gap-3 rounded-lg border border-slate-200 p-3">
                  <span className="rounded-md bg-slate-100 px-2 py-1 text-center text-xs font-bold">{label}</span>
                  <span className="text-sm font-semibold text-slate-700">{text}</span>
                </div>
              ))}
            </div>
            <div className="grid grid-cols-2 gap-3">
              {["Ollama ready", "Elasticsearch indexed", "Kafka streaming", "PostgreSQL healthy"].map((item) => (
                <div key={item} className="flex items-center gap-2 rounded-lg bg-white/[0.08] p-3 text-sm font-semibold text-slate-100">
                  <CheckCircle2 size={16} className="text-teal-300" />
                  {item}
                </div>
              ))}
            </div>
          </div>
        </div>
      </div>
      <div className="absolute bottom-14 right-14 rounded-lg border border-slate-200 bg-white p-4 shadow-xl">
        <div className="flex items-center gap-3">
          <Zap className="text-blue-700" size={20} />
          <div>
            <strong className="block text-sm text-slate-950">Action replay accepted</strong>
            <span className="text-xs text-slate-500">workflow execution refreshed</span>
          </div>
        </div>
      </div>
    </div>
  );
}
