create table attempts (
                          id bigserial primary key,
                          quiz_id bigint not null references quizzes(id) on delete cascade,
                          nickname varchar(60) not null,
                          score int not null default 0,
                          started_at timestamptz not null default now(),
                          finished_at timestamptz
);

create index idx_attempts_quiz_id on attempts(quiz_id);
create index idx_attempts_quiz_score on attempts(quiz_id, score desc);
