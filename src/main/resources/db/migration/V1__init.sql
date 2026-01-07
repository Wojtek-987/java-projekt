create table quizzes (
                         id bigserial primary key,
                         title varchar(200) not null,
                         description varchar(1000),
                         randomise_questions boolean not null default false,
                         randomise_answers boolean not null default false,
                         time_limit_seconds int,
                         negative_points_enabled boolean not null default false,
                         created_at timestamptz not null default now()
);

create table questions (
                           id bigserial primary key,
                           quiz_id bigint not null references quizzes(id) on delete cascade,
                           type varchar(50) not null,
                           prompt varchar(2000) not null,
                           points int not null default 1,
                           created_at timestamptz not null default now()
);

create index idx_questions_quiz_id on questions(quiz_id);
