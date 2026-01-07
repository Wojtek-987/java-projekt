create table tags (
                      id bigserial primary key,
                      name varchar(80) not null unique
);

create table quiz_tags (
                           quiz_id bigint not null references quizzes(id) on delete cascade,
                           tag_id bigint not null references tags(id) on delete cascade,
                           primary key (quiz_id, tag_id)
);
