create table tenants
(
    id         uuid         not null,
    slug       varchar(80)  not null,
    name       varchar(160) not null,
    status     varchar(30)  not null,
    created_at timestamptz  not null default now(),
    updated_at timestamptz  not null default now(),

    constraint pk_tenants primary key (id),
    constraint ux_tenants_slug unique (slug),
    constraint chk_tenants_status check (status in ('ACTIVE', 'SUSPENDED'))
);

create index idx_tenants_status_created_at
    on tenants (status, created_at desc);