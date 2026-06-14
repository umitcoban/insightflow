create unique index ux_automation_executions_tenant_rule_source_event
    on automation_executions (tenant_id, rule_id, source_event_id);
