import { FormEvent } from "react";
import { useParams } from "react-router";
import { Save, Users } from "lucide-react";
import { Button } from "../../components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "../../components/ui/card";
import { EmptyState } from "../../components/ui/empty-state";
import { Field, Input } from "../../components/ui/input";
import { PageHeader } from "../../components/ui/page-header";
import { useToast } from "../../components/ui/toast";
import { toMessage } from "../../lib/errors";
import { useFeedbacks } from "../feedback/hooks";
import { useCustomer, useCustomerMutations } from "./hooks";

export function CustomerDetailPage() {
  const { customerId } = useParams();
  const customer = useCustomer(customerId);
  const feedbacks = useFeedbacks({ customerId });
  const mutations = useCustomerMutations();
  const { notify } = useToast();

  async function update(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const form = new FormData(event.currentTarget);
    try {
      await mutations.update.mutateAsync({ id: customerId!, body: {
        externalId: form.get("externalId")?.toString(),
        email: form.get("email")?.toString(),
        fullName: form.get("fullName")?.toString(),
        plan: form.get("plan")?.toString()
      } });
      notify("Customer updated");
    } catch (error) {
      notify("Customer update failed", toMessage(error));
    }
  }

  const item = customer.data;
  return (
    <>
      <PageHeader eyebrow="Customer detail" title={item?.fullName ?? "Loading customer"} description="Edit the profile and inspect related feedback." />
      {item ? (
        <div className="grid gap-5 xl:grid-cols-[420px_1fr]">
          <Card>
            <CardHeader><CardTitle>Profile</CardTitle></CardHeader>
            <CardContent>
              <form className="grid gap-4" onSubmit={update}>
                <Field label="External ID"><Input name="externalId" defaultValue={item.externalId} /></Field>
                <Field label="Email"><Input name="email" type="email" defaultValue={item.email} /></Field>
                <Field label="Full name"><Input name="fullName" defaultValue={item.fullName} /></Field>
                <Field label="Plan"><Input name="plan" defaultValue={item.plan} /></Field>
                <Button variant="secondary"><Save size={16} /> Save profile</Button>
                {item.status === "INACTIVE" ? <Button type="button" onClick={() => mutations.activate.mutate(item.id)}>Activate</Button> : <Button type="button" variant="danger" onClick={() => mutations.deactivate.mutate(item.id)}>Deactivate</Button>}
              </form>
            </CardContent>
          </Card>
          <Card>
            <CardHeader><CardTitle>Feedback history</CardTitle></CardHeader>
            <CardContent className="grid gap-3">
              {feedbacks.data?.content.length ? feedbacks.data.content.map((feedback) => (
                <div key={feedback.id} className="rounded-lg border border-slate-200 p-3">
                  <strong>{feedback.title}</strong>
                  <p className="mt-1 text-sm text-slate-600">{feedback.aiSummary ?? feedback.content}</p>
                </div>
              )) : <EmptyState icon={<Users />} title="No related feedback" text="Feedback linked to this customer will appear here." />}
            </CardContent>
          </Card>
        </div>
      ) : <EmptyState icon={<Users />} title="Customer not found" text="The selected customer could not be loaded." />}
    </>
  );
}
