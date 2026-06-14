alter table automation_executions
    drop constraint chk_automation_executions_status;

alter table automation_executions
    add constraint chk_automation_executions_status
        check (status in ('PENDING', 'IN_PROGRESS', 'RETRY_SCHEDULED', 'SUCCESS', 'FAILED', 'SKIPPED'));

alter table automation_action_executions
    drop constraint chk_automation_action_executions_status;

alter table automation_action_executions
    add column attempt_count integer not null default 0,
    add column max_attempts integer not null default 3,
    add column next_retry_at timestamptz,
    add column last_attempt_at timestamptz,
    add column completed_at timestamptz;

update automation_action_executions
set attempt_count = 1,
    max_attempts = 1,
    completed_at = created_at
where status in ('SUCCESS', 'FAILED');

alter table automation_action_executions
    add constraint chk_automation_action_executions_status
        check (status in ('PENDING', 'IN_PROGRESS', 'RETRY_SCHEDULED', 'SUCCESS', 'FAILED', 'SKIPPED'));

create index idx_automation_action_executions_status_next_retry_at
    on automation_action_executions (status, next_retry_at);

create index idx_automation_action_executions_tenant_status_next_retry_at
    on automation_action_executions (tenant_id, status, next_retry_at);
