alter table questions
    add column if not exists options jsonb,
    add column if not exists answer_key jsonb not null default '{}'::jsonb;
