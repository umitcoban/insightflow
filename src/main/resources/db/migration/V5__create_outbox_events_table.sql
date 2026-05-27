create table outbox_events
(
    id              uuid         not null,
    tenant_id       uuid,
    aggregate_type  varchar(120) not null,
    aggregate_id    uuid         not null,
    event_type      varchar(160) not null,
    event_version   integer      not null,
    payload         jsonb        not null,
    status          varchar(40)  not null,
    retry_count     integer      not null default 0,
    next_retry_at   timestamptz,
    last_error      text,
    created_at      timestamptz  not null default now(),
    published_at    timestamptz,

    constraint pk_outbox_events primary key (id),
    constraint fk_outbox_events_tenant foreign key (tenant_id) references tenants (id),
    constraint chk_outbox_events_status check (status in ('PENDING', 'PUBLISHED', 'FAILED'))
);

create index idx_outbox_events_status_created_at
    on outbox_events (status, created_at);

create index idx_outbox_events_status_next_retry_at
    on outbox_events (status, next_retry_at);

create index idx_outbox_events_tenant_created_at
    on outbox_events (tenant_id, created_at desc);

create index idx_outbox_events_aggregate
    on outbox_events (aggregate_type, aggregate_id);