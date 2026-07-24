import { createBrowserRouter, Navigate } from "react-router";
import { RequireAuth } from "../auth/RequireAuth";
import { LoginPage } from "../auth/LoginPage";
import { AppLayout } from "../layouts/AppLayout";
import { MarketingLayout } from "../layouts/MarketingLayout";
import { LandingPage } from "../pages/landing/LandingPage";
import { DashboardPage } from "../pages/dashboard/DashboardPage";
import { OperationsPage } from "../pages/operations/OperationsPage";
import { FeedbackListPage } from "../features/feedback/FeedbackListPage";
import { FeedbackDetailPage } from "../features/feedback/FeedbackDetailPage";
import { CustomersPage } from "../features/customers/CustomersPage";
import { CustomerDetailPage } from "../features/customers/CustomerDetailPage";
import { KnowledgePage } from "../features/knowledge/KnowledgePage";
import { AssistantPage } from "../features/knowledge/AssistantPage";
import { AutomationRulesPage } from "../features/automation/AutomationRulesPage";
import { AutomationRuleDetailPage } from "../features/automation/AutomationRuleDetailPage";
import { AutomationPlaygroundPage } from "../features/automation/playground/AutomationPlaygroundPage";
import { TenantSettingsPage } from "../pages/settings/TenantSettingsPage";

export const router = createBrowserRouter([
  {
    element: <MarketingLayout />,
    children: [
      { path: "/", element: <LandingPage /> },
      { path: "/login", element: <LoginPage /> }
    ]
  },
  {
    path: "/app",
    element: (
      <RequireAuth>
        <AppLayout />
      </RequireAuth>
    ),
    children: [
      { index: true, element: <Navigate to="/app/dashboard" replace /> },
      { path: "dashboard", element: <DashboardPage /> },
      { path: "feedback", element: <FeedbackListPage /> },
      { path: "feedback/:feedbackId", element: <FeedbackDetailPage /> },
      { path: "customers", element: <CustomersPage /> },
      { path: "customers/:customerId", element: <CustomerDetailPage /> },
      { path: "assistant", element: <AssistantPage /> },
      { path: "knowledge", element: <KnowledgePage /> },
      { path: "automation", element: <AutomationRulesPage /> },
      { path: "automation/playground", element: <AutomationPlaygroundPage /> },
      { path: "automation/:ruleId", element: <AutomationRuleDetailPage /> },
      { path: "operations", element: <OperationsPage /> },
      { path: "settings/tenant", element: <TenantSettingsPage /> }
    ]
  },
  { path: "*", element: <Navigate to="/" replace /> }
]);
