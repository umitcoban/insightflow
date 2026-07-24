import { FormEvent, useState } from "react";
import { Link } from "react-router";
import { Plus, Users } from "lucide-react";
import { Badge } from "../../components/ui/badge";
import { Button } from "../../components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "../../components/ui/card";
import { EmptyState } from "../../components/ui/empty-state";
import { Field, Input } from "../../components/ui/input";
import { PageHeader } from "../../components/ui/page-header";
import { useToast } from "../../components/ui/toast";
import { toMessage } from "../../lib/errors";
import { useCustomerMutations, useCustomers } from "./hooks";

export function CustomersPage() {
  const [query, setQuery] = useState("");
  const customers = useCustomers(query);
  const mutations = useCustomerMutations();
  const { notify } = useToast();

  async function createCustomer(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const formElement = event.currentTarget;
    const form = new FormData(formElement);
    try {
      await mutations.create.mutateAsync({
        externalId: form.get("externalId")?.toString(),
        email: form.get("email")?.toString(),
        fullName: form.get("fullName")?.toString(),
        plan: form.get("plan")?.toString()
      });
      formElement.reset();
      notify("Customer created");
    } catch (error) {
      notify("Customer create failed", toMessage(error));
    }
  }

  return (
    <>
      <PageHeader eyebrow="Customers" title="Customer directory" description="Manage tenant-scoped customers and inspect their feedback history." />
      <div className="grid gap-5 xl:grid-cols-[1fr_420px]">
        <Card>
          <CardHeader><CardTitle>Customers</CardTitle><Input className="max-w-xs" value={query} onChange={(event) => setQuery(event.target.value)} placeholder="Search customers" /></CardHeader>
          <CardContent className="grid gap-3">
            {customers.data?.content.length ? customers.data.content.map((customer) => (
              <Link key={customer.id} to={`/app/customers/${customer.id}`} className="flex items-center justify-between rounded-lg border border-slate-200 p-4 hover:bg-slate-50">
                <div><strong>{customer.fullName}</strong><p className="text-sm text-slate-600">{customer.email}</p></div>
                <div className="flex gap-2"><Badge>{customer.plan}</Badge><Badge>{customer.status ?? "ACTIVE"}</Badge></div>
              </Link>
            )) : <EmptyState icon={<Users />} title="No customers" text="Create the first customer for this tenant." />}
          </CardContent>
        </Card>
        <Card>
          <CardHeader><CardTitle>Create Customer</CardTitle></CardHeader>
          <CardContent>
            <form className="grid gap-4" onSubmit={createCustomer}>
              <Field label="External ID"><Input name="externalId" required /></Field>
              <Field label="Email"><Input name="email" required type="email" /></Field>
              <Field label="Full name"><Input name="fullName" required /></Field>
              <Field label="Plan"><Input name="plan" required placeholder="PREMIUM" /></Field>
              <Button><Plus size={16} /> Create customer</Button>
            </form>
          </CardContent>
        </Card>
      </div>
    </>
  );
}
