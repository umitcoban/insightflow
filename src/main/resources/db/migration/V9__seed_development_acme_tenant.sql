insert into tenants (id, slug, name, status, created_at, updated_at)
values (
    '11111111-1111-1111-1111-111111111111',
    'acme',
    'Acme Development',
    'ACTIVE',
    now(),
    now()
)
on conflict (slug) do nothing;
