alter table customers
    add column status varchar(40) not null default 'ACTIVE',
    add column deactivated_at timestamptz,
    add constraint chk_customers_status check (status in ('ACTIVE', 'INACTIVE'));

create index idx_customers_tenant_status_created_at
    on customers (tenant_id, status, created_at desc);

alter table feedbacks
    add column assigned_to varchar(180),
    add column archived_at timestamptz;

create table feedback_notes
(
    id          uuid        not null,
    tenant_id   uuid        not null,
    feedback_id uuid        not null,
    author      varchar(180) not null,
    content     text        not null,
    created_at  timestamptz not null default now(),

    constraint pk_feedback_notes primary key (id),
    constraint fk_feedback_notes_tenant foreign key (tenant_id) references tenants (id),
    constraint fk_feedback_notes_feedback foreign key (feedback_id) references feedbacks (id)
);

create index idx_feedback_notes_tenant_feedback_created_at
    on feedback_notes (tenant_id, feedback_id, created_at asc);

create table tenant_settings
(
    tenant_id  uuid        not null,
    settings   jsonb       not null default '{}'::jsonb,
    updated_at timestamptz not null default now(),

    constraint pk_tenant_settings primary key (tenant_id),
    constraint fk_tenant_settings_tenant foreign key (tenant_id) references tenants (id)
);

create table knowledge_documents
(
    id          uuid         not null,
    tenant_id   uuid         not null,
    title       varchar(240) not null,
    source      varchar(80)  not null,
    content     text         not null,
    metadata    jsonb        not null default '{}'::jsonb,
    created_at  timestamptz  not null default now(),
    updated_at  timestamptz  not null default now(),

    constraint pk_knowledge_documents primary key (id),
    constraint fk_knowledge_documents_tenant foreign key (tenant_id) references tenants (id)
);

create index idx_knowledge_documents_tenant_created_at
    on knowledge_documents (tenant_id, created_at desc);

create table knowledge_chunks
(
    id          uuid        not null,
    tenant_id   uuid        not null,
    document_id uuid        not null,
    chunk_index integer     not null,
    content     text        not null,
    created_at  timestamptz not null default now(),

    constraint pk_knowledge_chunks primary key (id),
    constraint fk_knowledge_chunks_tenant foreign key (tenant_id) references tenants (id),
    constraint fk_knowledge_chunks_document foreign key (document_id) references knowledge_documents (id),
    constraint ux_knowledge_chunks_document_index unique (document_id, chunk_index)
);

create index idx_knowledge_chunks_tenant_document
    on knowledge_chunks (tenant_id, document_id);

