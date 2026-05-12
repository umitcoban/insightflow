create table customers
(
    id          uuid         not null,
    tenant_id   uuid         not null,
    external_id varchar(120),
    email       varchar(254),
    full_name   varchar(180),
    plan        varchar(60),
    created_at  timestamptz  not null default now(),
    updated_at  timestamptz  not null default now(),

    constraint pk_customers primary key (id),
    constraint fk_customers_tenant foreign key (tenant_id) references tenants (id),
    constraint ux_customers_tenant_external_id unique (tenant_id, external_id),
    constraint ux_customers_tenant_email unique (tenant_id, email)
);

create index idx_customers_tenant_created_at
    on customers (tenant_id, created_at desc);

create table feedbacks
(
    id               uuid         not null,
    tenant_id         uuid         not null,
    customer_id       uuid,
    source            varchar(40)  not null,
    title             varchar(200) not null,
    content           text         not null,
    status            varchar(40)  not null,
    priority          varchar(40)  not null,
    sentiment         varchar(40),
    category          varchar(80),
    risk_level        varchar(40),
    ai_summary        text,
    suggested_action  text,
    metadata          jsonb        not null default '{}'::jsonb,
    created_at        timestamptz  not null default now(),
    updated_at        timestamptz  not null default now(),

    constraint pk_feedbacks primary key (id),
    constraint fk_feedbacks_tenant foreign key (tenant_id) references tenants (id),
    constraint fk_feedbacks_customer foreign key (customer_id) references customers (id),

    constraint chk_feedbacks_source check (source in ('MANUAL', 'API', 'EMAIL', 'APP_REVIEW')),
    constraint chk_feedbacks_status check (status in ('NEW', 'IN_REVIEW', 'RESOLVED', 'ARCHIVED')),
    constraint chk_feedbacks_priority check (priority in ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL')),
    constraint chk_feedbacks_sentiment check (sentiment is null or sentiment in ('POSITIVE', 'NEUTRAL', 'NEGATIVE')),
    constraint chk_feedbacks_risk_level check (risk_level is null or risk_level in ('LOW', 'MEDIUM', 'HIGH', 'CHURN_RISK'))
);

create index idx_feedbacks_tenant_status_created_at
    on feedbacks (tenant_id, status, created_at desc);

create index idx_feedbacks_tenant_priority_created_at
    on feedbacks (tenant_id, priority, created_at desc);

create index idx_feedbacks_tenant_category_created_at
    on feedbacks (tenant_id, category, created_at desc);

create index idx_feedbacks_tenant_customer_created_at
    on feedbacks (tenant_id, customer_id, created_at desc);

create index idx_feedbacks_tenant_created_at
    on feedbacks (tenant_id, created_at desc);

create index idx_feedbacks_metadata_gin
    on feedbacks using gin (metadata);