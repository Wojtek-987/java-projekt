create table attempt_answers (
                                 id bigserial primary key,
                                 attempt_id bigint not null references attempts(id) on delete cascade,
                                 question_id bigint not null references questions(id) on delete cascade,
                                 answer jsonb not null,
                                 is_correct boolean not null,
                                 awarded_points int not null,
                                 answered_at timestamptz not null default now(),
                                 unique (attempt_id, question_id)
);

create index idx_attempt_answers_attempt on attempt_answers(attempt_id);
