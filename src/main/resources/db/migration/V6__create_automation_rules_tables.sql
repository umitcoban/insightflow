create table automation_rules
(
    id                 uuid         not null,
    tenant_id          uuid         not null,
    name               varchar(160) not null,
    description        text,
    trigger_event_type varchar(160) not null,
    condition_json     jsonb        not null,
    action_json        jsonb        not null,
    status             varchar(40)  not null,
    priority           integer      not null default 100,
    created_at         timestamptz  not null default now(),
    updated_at         timestamptz  not null default now(),

    constraint pk_automation_rules primary key (id),
    constraint fk_automation_rules_tenant foreign key (tenant_id) references tenants (id),
    constraint chk_automation_rules_status check (status in ('ACTIVE', 'INACTIVE'))
);

create index idx_automation_rules_tenant_trigger_status_priority
    on automation_rules (tenant_id, trigger_event_type, status, priority asc);

create index idx_automation_rules_tenant_created_at
    on automation_rules (tenant_id, created_at desc);

create index idx_automation_rules_condition_gin
    on automation_rules using gin (condition_json);

create table automation_executions
(
    id                uuid        not null,
    tenant_id         uuid        not null,
    rule_id           uuid        not null,
    source_event_id   uuid        not null,
    source_event_type varchar(160) not null,
    status            varchar(40) not null,
    matched           boolean     not null,
    error_message     text,
    started_at        timestamptz not null default now(),
    finished_at       timestamptz,

    constraint pk_automation_executions primary key (id),
    constraint fk_automation_executions_tenant foreign key (tenant_id) references tenants (id),
    constraint fk_automation_executions_rule foreign key (rule_id) references automation_rules (id),
    constraint chk_automation_executions_status check (status in ('SUCCESS', 'FAILED', 'SKIPPED'))
);

create index idx_automation_executions_tenant_started_at
    on automation_executions (tenant_id, started_at desc);

create index idx_automation_executions_rule_started_at
    on automation_executions (rule_id, started_at desc);

create index idx_automation_executions_source_event
    on automation_executions (source_event_id);

create table automation_action_executions
(
    id              uuid        not null,
    tenant_id       uuid        not null,
    execution_id    uuid        not null,
    action_type     varchar(80) not null,
    status          varchar(40) not null,
    request_payload jsonb       not null default '{}'::jsonb,
    result_payload  jsonb       not null default '{}'::jsonb,
    error_message   text,
    created_at      timestamptz not null default now(),

    constraint pk_automation_action_executions primary key (id),
    constraint fk_automation_action_executions_tenant foreign key (tenant_id) references tenants (id),
    constraint fk_automation_action_executions_execution foreign key (execution_id) references automation_executions (id),
    constraint chk_automation_action_executions_status check (status in ('SUCCESS', 'FAILED', 'SKIPPED'))
);

create index idx_automation_action_executions_execution_created_at
    on automation_action_executions (execution_id, created_at asc);

create index idx_automation_action_executions_tenant_created_at
    on automation_action_executions (tenant_id, created_at desc);