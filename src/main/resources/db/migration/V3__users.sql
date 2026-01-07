create table users (
                       id bigserial primary key,
                       email varchar(320) not null unique,
                       password_hash varchar(100) not null,
                       role varchar(30) not null,
                       created_at timestamptz not null default now()
);
