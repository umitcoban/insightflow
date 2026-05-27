alter table customers
    add constraint ux_customers_tenant_id_id unique (tenant_id, id);

alter table feedbacks
    drop constraint fk_feedbacks_customer;

alter table feedbacks
    add constraint fk_feedbacks_customer_same_tenant
        foreign key (tenant_id, customer_id)
            references customers (tenant_id, id);