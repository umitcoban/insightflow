import React, { FormEvent, useEffect, useState } from "react";
import { createRoot } from "react-dom/client";
import {
  Activity,
  AlertCircle,
  ArrowRight,
  Bot,
  CheckCircle2,
  CircleDot,
  ClipboardList,
  Command,
  Database,
  FileText,
  Gauge,
  LogOut,
  MessageSquareText,
  Plus,
  RefreshCcw,
  Search,
  ShieldCheck,
  Sparkles,
  Trash2,
  Users,
  Zap
} from "lucide-react";
import {
  api,
  ApiError,
  Customer,
  demoUsers,
  Feedback,
  HealthResponse,
  login,
  Session,
  AutomationRule,
  AutomationExecution,
  KnowledgeDocument,
  AssistantAnswer
} from "./api";
import { clearSession, loadSession, saveSession } from "./storage";
import "./styles.css";

type View = "overview" | "feedback" | "customers" | "assistant" | "automation" | "operations";

type AppData = {
  health: HealthResponse | null;
  readiness: HealthResponse | null;
  customers: Customer[];
  feedbacks: Feedback[];
  searchResults: Feedback[];
  rules: AutomationRule[];
  executions: AutomationExecution[];
  documents: KnowledgeDocument[];
};

const emptyData: AppData = {
  health: null,
  readiness: null,
  customers: [],
  feedbacks: [],
  searchResults: [],
  rules: [],
  executions: [],
  documents: []
};

function App() {
  const [session, setSession] = useState<Session | null>(() => loadSession());
  const [view, setView] = useState<View>("overview");
  const [data, setData] = useState<AppData>(emptyData);
  const [loading, setLoading] = useState(false);
  const [notice, setNotice] = useState<string | null>(null);

  async function refresh() {
    setLoading(true);
    setNotice(null);
    try {
      const [healthResult, readinessResult] = await Promise.allSettled([api.health(), api.readiness()]);
      const health = healthResult.status === "fulfilled" ? healthResult.value : null;
      const readiness = readinessResult.status === "fulfilled" ? readinessResult.value : null;
      if (!session) {
        setData((current) => ({ ...current, health, readiness }));
        return;
      }
      const [customers, feedbacks, rules, executions, documents] = await Promise.all([
        api.customers(session),
        api.feedbacks(session),
        api.automationRules(session),
        api.automationExecutions(session),
        api.knowledgeDocuments(session)
      ]);
      setData({
        health,
        readiness,
        customers: customers.content,
        feedbacks: feedbacks.content,
        searchResults: [],
        rules: rules.content,
        executions: executions.content,
        documents: documents.content
      });
    } catch (error) {
      setNotice(toMessage(error));
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    void refresh();
  }, [session?.accessToken]);

  function handleSession(next: Session) {
    saveSession(next);
    setSession(next);
  }

  function signOut() {
    clearSession();
    setSession(null);
    setView("overview");
    setData(emptyData);
  }

  return (
    <div className="app-shell">
      <Sidebar active={view} onNavigate={setView} />
      <main className="workspace">
        <Topbar session={session} loading={loading} onRefresh={refresh} onSignOut={signOut} />
        {notice ? <Banner message={notice} onClose={() => setNotice(null)} /> : null}
        {!session ? <LoginPanel onLogin={handleSession} health={data.health} /> : null}
        {session ? (
          <>
            {view === "overview" ? <Overview data={data} session={session} /> : null}
            {view === "feedback" ? <FeedbackView session={session} data={data} setData={setData} setNotice={setNotice} /> : null}
            {view === "customers" ? <CustomersView session={session} data={data} setData={setData} setNotice={setNotice} /> : null}
            {view === "assistant" ? <AssistantView session={session} data={data} setData={setData} setNotice={setNotice} /> : null}
            {view === "automation" ? <AutomationView session={session} data={data} setData={setData} setNotice={setNotice} /> : null}
            {view === "operations" ? <OperationsView data={data} /> : null}
          </>
        ) : null}
      </main>
    </div>
  );
}

function Sidebar({ active, onNavigate }: { active: View; onNavigate: (view: View) => void }) {
  const items = [
    { id: "overview" as const, label: "Overview", icon: Gauge },
    { id: "feedback" as const, label: "Feedback", icon: MessageSquareText },
    { id: "customers" as const, label: "Customers", icon: Users },
    { id: "assistant" as const, label: "Assistant", icon: Bot },
    { id: "automation" as const, label: "Automation", icon: Zap },
    { id: "operations" as const, label: "Operations", icon: Activity }
  ];
  return (
    <aside className="sidebar">
      <div className="brand">
        <div className="brand-mark">
          <Command size={21} />
        </div>
        <div>
          <strong>InsightFlow</strong>
          <span>Customer intelligence</span>
        </div>
      </div>
      <nav className="nav-list">
        {items.map((item) => {
          const Icon = item.icon;
          return (
            <button key={item.id} className={active === item.id ? "nav-item active" : "nav-item"} onClick={() => onNavigate(item.id)}>
              <Icon size={18} />
              <span>{item.label}</span>
            </button>
          );
        })}
      </nav>
      <div className="sidebar-note">
        <ShieldCheck size={18} />
        <span>Tenant scoped by JWT and header</span>
      </div>
    </aside>
  );
}

function Topbar({
  session,
  loading,
  onRefresh,
  onSignOut
}: {
  session: Session | null;
  loading: boolean;
  onRefresh: () => void;
  onSignOut: () => void;
}) {
  return (
    <header className="topbar">
      <div>
        <p className="eyebrow">Operational Console</p>
        <h1>Feedback, customers, automation and knowledge in one control room.</h1>
      </div>
      <div className="topbar-actions">
        {session ? (
          <div className="session-pill">
            <span>{session.tenantSlug}</span>
            <strong>{session.username}</strong>
          </div>
        ) : null}
        <button className="icon-button" onClick={onRefresh} title="Refresh data">
          <RefreshCcw size={18} className={loading ? "spin" : ""} />
        </button>
        {session ? (
          <button className="icon-button" onClick={onSignOut} title="Sign out">
            <LogOut size={18} />
          </button>
        ) : null}
      </div>
    </header>
  );
}

function LoginPanel({ onLogin, health }: { onLogin: (session: Session) => void; health: HealthResponse | null }) {
  const [selected, setSelected] = useState(demoUsers[0]);
  const [tenantSlug, setTenantSlug] = useState("acme");
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<string | null>(null);

  async function submit(event: FormEvent) {
    event.preventDefault();
    setBusy(true);
    setError(null);
    try {
      onLogin(await login(selected.username, selected.password, selected.role, tenantSlug));
    } catch (loginError) {
      setError(toMessage(loginError));
    } finally {
      setBusy(false);
    }
  }

  return (
    <section className="login-grid">
      <div className="login-copy">
        <div className="status-row">
          <StatusDot status={health?.status ?? "UNKNOWN"} />
          <span>API health: {health?.status ?? "not checked"}</span>
        </div>
        <h2>Start with a development user.</h2>
        <p>
          The frontend uses the local Keycloak dev client, then talks to the Spring Boot API with the same tenant boundary
          expected by the backend.
        </p>
        <div className="feature-list">
          <span><Database size={16} /> PostgreSQL backed customers</span>
          <span><Sparkles size={16} /> Spring AI and Ollama enrichment</span>
          <span><ClipboardList size={16} /> Automation execution history</span>
        </div>
      </div>
      <form className="form-card login-card" onSubmit={submit}>
        <label>
          Demo identity
          <select value={selected.username} onChange={(event) => setSelected(demoUsers.find((user) => user.username === event.target.value) ?? demoUsers[0])}>
            {demoUsers.map((user) => (
              <option key={user.username} value={user.username}>
                {user.username} · {user.role}
              </option>
            ))}
          </select>
        </label>
        <label>
          Tenant slug
          <input value={tenantSlug} onChange={(event) => setTenantSlug(event.target.value)} />
        </label>
        {error ? <p className="form-error">{error}</p> : null}
        <button className="primary-button" disabled={busy}>
          {busy ? "Connecting..." : "Enter console"} <ArrowRight size={17} />
        </button>
      </form>
    </section>
  );
}

function Overview({ data, session }: { data: AppData; session: Session }) {
  const urgent = data.feedbacks.filter((item) => ["HIGH", "CRITICAL"].includes(item.priority)).length;
  const enriched = data.feedbacks.filter((item) => item.aiSummary || item.sentiment).length;
  return (
    <section className="content-stack">
      <div className="metric-grid">
        <Metric title="Customers" value={data.customers.length} icon={<Users size={20} />} tone="teal" />
        <Metric title="Open feedback" value={data.feedbacks.filter((item) => item.status !== "RESOLVED").length} icon={<MessageSquareText size={20} />} tone="blue" />
        <Metric title="Urgent items" value={urgent} icon={<AlertCircle size={20} />} tone="amber" />
        <Metric title="AI enriched" value={enriched} icon={<Sparkles size={20} />} tone="green" />
      </div>
      <div className="split-grid">
        <Panel title="Recent Feedback" action="Tenant scoped">
          <FeedbackList items={data.feedbacks.slice(0, 5)} />
        </Panel>
        <Panel title="Automation Pulse" action={session.role}>
          <div className="timeline">
            {data.executions.length ? data.executions.slice(0, 6).map((item) => (
              <div className="timeline-item" key={item.id}>
                <CircleDot size={16} />
                <div>
                  <strong>{item.sourceEventType}</strong>
                  <span>{item.status} · {shortId(item.ruleId)}</span>
                </div>
              </div>
            )) : <EmptyState icon={<Zap size={22} />} title="No executions yet" text="Create matching feedback after a rule is active." />}
          </div>
        </Panel>
      </div>
    </section>
  );
}

function FeedbackView({
  session,
  data,
  setData,
  setNotice
}: {
  session: Session;
  data: AppData;
  setData: React.Dispatch<React.SetStateAction<AppData>>;
  setNotice: (message: string | null) => void;
}) {
  const [query, setQuery] = useState("");
  const [creating, setCreating] = useState(false);
  const visible = query ? data.searchResults : data.feedbacks;

  async function search() {
    if (!query.trim()) {
      setData((current) => ({ ...current, searchResults: [] }));
      return;
    }
    try {
      const result = await api.searchFeedbacks(session, query);
      setData((current) => ({ ...current, searchResults: result.content }));
    } catch (error) {
      setNotice(toMessage(error));
    }
  }

  async function createSample() {
    setCreating(true);
    try {
      const item = await api.createFeedback(session, {
        source: "MANUAL",
        title: "Checkout screen freezes",
        content: "Customer reports that checkout freezes after tapping pay on the latest mobile app build.",
        priority: "HIGH",
        metadata: { channel: "support", platform: "ios" }
      });
      setData((current) => ({ ...current, feedbacks: [item, ...current.feedbacks] }));
      setNotice("Feedback created. AI enrichment will arrive through the event loop.");
    } catch (error) {
      setNotice(toMessage(error));
    } finally {
      setCreating(false);
    }
  }

  return (
    <section className="content-stack">
      <div className="toolbar">
        <div className="search-box">
          <Search size={18} />
          <input value={query} onChange={(event) => setQuery(event.target.value)} onKeyDown={(event) => event.key === "Enter" && void search()} placeholder="Search feedback by title, customer signal, category..." />
        </div>
        <button className="secondary-button" onClick={search}>Search</button>
        <button className="primary-button" onClick={createSample} disabled={creating}>
          <Plus size={17} /> Create sample
        </button>
      </div>
      <Panel title={query ? "Search Results" : "Feedback Queue"} action={`${visible.length} visible`}>
        <FeedbackList items={visible} />
      </Panel>
    </section>
  );
}

function CustomersView({
  session,
  data,
  setData,
  setNotice
}: {
  session: Session;
  data: AppData;
  setData: React.Dispatch<React.SetStateAction<AppData>>;
  setNotice: (message: string | null) => void;
}) {
  async function createCustomer(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const formElement = event.currentTarget;
    const form = new FormData(formElement);
    try {
      const customer = await api.createCustomer(session, {
        externalId: form.get("externalId")?.toString(),
        email: form.get("email")?.toString(),
        fullName: form.get("fullName")?.toString(),
        plan: form.get("plan")?.toString()
      });
      formElement.reset();
      setData((current) => ({ ...current, customers: [customer, ...current.customers] }));
      setNotice("Customer created.");
    } catch (error) {
      setNotice(toMessage(error));
    }
  }

  return (
    <section className="content-stack">
      <div className="split-grid">
        <Panel title="Customer Directory" action={`${data.customers.length} records`}>
          <div className="table-list">
            {data.customers.length ? data.customers.map((customer) => (
              <div className="table-row" key={customer.id}>
                <div>
                  <strong>{customer.fullName}</strong>
                  <span>{customer.email}</span>
                </div>
                <Badge>{customer.plan}</Badge>
              </div>
            )) : <EmptyState icon={<Users size={22} />} title="No customers" text="Create the first customer for this tenant." />}
          </div>
        </Panel>
        <form className="form-card" onSubmit={createCustomer}>
          <h3>Add Customer</h3>
          <label>External ID<input name="externalId" required placeholder="customer-1024" /></label>
          <label>Email<input name="email" required type="email" placeholder="customer@example.com" /></label>
          <label>Full name<input name="fullName" required placeholder="Jane Doe" /></label>
          <label>Plan<input name="plan" required placeholder="PREMIUM" /></label>
          <button className="primary-button"><Plus size={17} /> Create customer</button>
        </form>
      </div>
    </section>
  );
}

function AssistantView({
  session,
  data,
  setData,
  setNotice
}: {
  session: Session;
  data: AppData;
  setData: React.Dispatch<React.SetStateAction<AppData>>;
  setNotice: (message: string | null) => void;
}) {
  const [answer, setAnswer] = useState<AssistantAnswer | null>(null);
  const [asking, setAsking] = useState(false);

  async function createDocument(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const formElement = event.currentTarget;
    const form = new FormData(formElement);
    try {
      const document = await api.createKnowledgeDocument(session, {
        title: form.get("title")?.toString(),
        source: form.get("source")?.toString(),
        content: form.get("content")?.toString()
      });
      formElement.reset();
      setData((current) => ({ ...current, documents: [document, ...current.documents] }));
      setNotice("Knowledge document indexed.");
    } catch (error) {
      setNotice(toMessage(error));
    }
  }
  
  async function deleteDocument(documentId: string) {
    try {
      await api.deleteKnowledgeDocument(session, documentId);
      setData((current) => ({
        ...current,
        documents: current.documents.filter((document) => document.id !== documentId)
      }));
      setNotice("Knowledge document deleted.");
    } catch (error) {
      setNotice(toMessage(error));
    }
  }

  async function ask(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const form = new FormData(event.currentTarget);
    setAsking(true);
    try {
      setAnswer(await api.askAssistant(session, form.get("question")?.toString() ?? ""));
    } catch (error) {
      setNotice(toMessage(error));
    } finally {
      setAsking(false);
    }
  }

  return (
    <section className="content-stack">
      <div className="split-grid">
        <Panel title="Knowledge Base" action={`${data.documents.length} docs`}>
          <div className="table-list compact">
            {data.documents.length ? data.documents.map((document) => (
              <div className="table-row" key={document.id}>
                <div>
                  <strong>{document.title}</strong>
                  <span>{document.source}</span>
                </div>
                <button className="row-icon-button" onClick={() => void deleteDocument(document.id)} title="Delete knowledge document">
                  <Trash2 size={16} />
                </button>
              </div>
            )) : <EmptyState icon={<FileText size={22} />} title="No documents" text="Add product context before asking tenant questions." />}
          </div>
        </Panel>
        <form className="form-card" onSubmit={createDocument}>
          <h3>Add Knowledge</h3>
          <label>Title<input name="title" required placeholder="Refund policy" /></label>
          <label>Source<input name="source" required placeholder="support-playbook" /></label>
          <label>Content<textarea name="content" required placeholder="Paste operational knowledge..." /></label>
          <button className="primary-button"><Plus size={17} /> Index document</button>
        </form>
      </div>
      <Panel title="Assistant" action="Tenant knowledge only">
        <form className="question-row" onSubmit={ask}>
          <input name="question" placeholder="Ask about policies, known issues, playbooks..." />
          <button className="primary-button" disabled={asking}>{asking ? "Thinking..." : "Ask"}</button>
        </form>
        {answer ? (
          <div className="answer-box">
            <strong>Answer</strong>
            <p>{answer.answer}</p>
            <div className="source-list">
              {answer.sources?.slice(0, 3).map((source) => <Badge key={source.chunkId}>{source.documentTitle}</Badge>)}
            </div>
          </div>
        ) : null}
      </Panel>
    </section>
  );
}

function AutomationView({
  session,
  data,
  setData,
  setNotice
}: {
  session: Session;
  data: AppData;
  setData: React.Dispatch<React.SetStateAction<AppData>>;
  setNotice: (message: string | null) => void;
}) {
  async function createRule() {
    try {
      const rule = await api.createAutomationRule(session, {
        name: "Log churn risk negative feedback",
        description: "Created from the web console.",
        triggerEventType: "feedback.ai-analysis-completed",
        conditionJson: {
          all: [
            { path: "sentiment", op: "eq", value: "NEGATIVE" },
            { path: "riskLevel", op: "in", value: ["HIGH", "CHURN_RISK"] }
          ]
        },
        actionJson: [{ type: "LOG", message: "High risk negative feedback detected" }],
        priority: 100
      });
      setData((current) => ({ ...current, rules: [rule, ...current.rules] }));
      setNotice("Automation rule created.");
    } catch (error) {
      setNotice(toMessage(error));
    }
  }

  return (
    <section className="content-stack">
      <div className="toolbar">
        <div>
          <h2>Automation Rules</h2>
          <p className="muted">Create simple event-driven controls for feedback enrichment.</p>
        </div>
        <button className="primary-button" onClick={createRule}><Plus size={17} /> Create sample rule</button>
      </div>
      <div className="split-grid">
        <Panel title="Rules" action={`${data.rules.length} configured`}>
          <div className="table-list">
            {data.rules.length ? data.rules.map((rule) => (
              <div className="table-row" key={rule.id}>
                <div>
                  <strong>{rule.name}</strong>
                  <span>{rule.triggerEventType} · priority {rule.priority}</span>
                </div>
                <Badge>{rule.status}</Badge>
              </div>
            )) : <EmptyState icon={<Zap size={22} />} title="No automation rules" text="Start with a sample rule, then tune it from API or later UI forms." />}
          </div>
        </Panel>
        <Panel title="Executions" action={`${data.executions.length} recent`}>
          <div className="timeline">
            {data.executions.length ? data.executions.map((execution) => (
              <div className="timeline-item" key={execution.id}>
                <CircleDot size={16} />
                <div>
                  <strong>{execution.status}</strong>
                  <span>{execution.sourceEventType} · {shortId(execution.id)}</span>
                </div>
              </div>
            )) : <EmptyState icon={<ClipboardList size={22} />} title="No executions" text="Executions appear after matching events are consumed." />}
          </div>
        </Panel>
      </div>
    </section>
  );
}

function OperationsView({ data }: { data: AppData }) {
  const components = data.health?.components ?? {};
  return (
    <section className="content-stack">
      <div className="metric-grid">
        <Metric title="Health" value={data.health?.status ?? "UNKNOWN"} icon={<Activity size={20} />} tone="green" />
        <Metric title="Readiness" value={data.readiness?.status ?? "UNKNOWN"} icon={<CheckCircle2 size={20} />} tone="teal" />
        <Metric title="Knowledge docs" value={data.documents.length} icon={<FileText size={20} />} tone="blue" />
        <Metric title="Rules" value={data.rules.length} icon={<Zap size={20} />} tone="amber" />
      </div>
      <Panel title="Health Components" action="Actuator">
        <div className="component-grid">
          {Object.entries(components).map(([name, component]) => (
            <div className="component-card" key={name}>
              <StatusDot status={component.status} />
              <strong>{name}</strong>
              <span>{component.status}</span>
            </div>
          ))}
        </div>
      </Panel>
    </section>
  );
}

function FeedbackList({ items }: { items: Feedback[] }) {
  if (!items.length) {
    return <EmptyState icon={<MessageSquareText size={22} />} title="No feedback yet" text="Create feedback or adjust your search filters." />;
  }
  return (
    <div className="feedback-list">
      {items.map((item) => (
        <article className="feedback-card" key={item.id}>
          <div className="feedback-main">
            <div>
              <strong>{item.title}</strong>
              <p>{item.aiSummary || item.content}</p>
            </div>
            <span className="short-id">{shortId(item.id)}</span>
          </div>
          <div className="chip-row">
            <Badge>{item.priority}</Badge>
            <Badge>{item.status}</Badge>
            {item.sentiment ? <Badge>{item.sentiment}</Badge> : null}
            {item.riskLevel ? <Badge>{item.riskLevel}</Badge> : null}
            {item.category ? <Badge>{item.category}</Badge> : null}
          </div>
        </article>
      ))}
    </div>
  );
}

function Metric({ title, value, icon, tone }: { title: string; value: React.ReactNode; icon: React.ReactNode; tone: string }) {
  return (
    <div className={`metric metric-${tone}`}>
      <div className="metric-icon">{icon}</div>
      <span>{title}</span>
      <strong>{value}</strong>
    </div>
  );
}

function Panel({ title, action, children }: { title: string; action?: string; children: React.ReactNode }) {
  return (
    <section className="panel">
      <div className="panel-header">
        <h2>{title}</h2>
        {action ? <span>{action}</span> : null}
      </div>
      {children}
    </section>
  );
}

function Badge({ children }: { children: React.ReactNode }) {
  return <span className="badge">{children}</span>;
}

function EmptyState({ icon, title, text }: { icon: React.ReactNode; title: string; text: string }) {
  return (
    <div className="empty-state">
      {icon}
      <strong>{title}</strong>
      <span>{text}</span>
    </div>
  );
}

function StatusDot({ status }: { status: string }) {
  return <span className={status === "UP" ? "status-dot up" : "status-dot down"} />;
}

function Banner({ message, onClose }: { message: string; onClose: () => void }) {
  return (
    <div className="banner">
      <AlertCircle size={18} />
      <span>{message}</span>
      <button onClick={onClose}>Dismiss</button>
    </div>
  );
}

function shortId(id?: string) {
  return id ? id.slice(0, 8) : "pending";
}

function toMessage(error: unknown) {
  if (error instanceof ApiError) {
    const detail = typeof error.details === "object" && error.details && "detail" in error.details
      ? String((error.details as { detail?: unknown }).detail)
      : error.message;
    return `${detail} (${error.status})`;
  }
  if (error instanceof Error) {
    return error.message;
  }
  return "Unexpected error";
}

createRoot(document.getElementById("root")!).render(
  <React.StrictMode>
    <App />
  </React.StrictMode>
);
