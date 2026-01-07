insert into quizzes (id, title, description, randomise_questions, randomise_answers, time_limit_seconds, negative_points_enabled)
values
    (1, 'Java Basics', 'Warm-up quiz for Java fundamentals', false, false, 120, false),
    (2, 'Spring Boot Intro', 'Starter-level Spring Boot questions', true, true, 180, true)
    on conflict (id) do update
                            set title = excluded.title,
                            description = excluded.description,
                            randomise_questions = excluded.randomise_questions,
                            randomise_answers = excluded.randomise_answers,
                            time_limit_seconds = excluded.time_limit_seconds,
                            negative_points_enabled = excluded.negative_points_enabled;

-- reset questions for dev
delete from questions where quiz_id in (1, 2);

-- quiz 1: all 8 types (required)
insert into questions (quiz_id, type, prompt, points, options, answer_key)
values
    (1, 'SINGLE_CHOICE', 'Entry point method name in Java?', 2,
     '["main","start","run"]'::jsonb,
     '{"value":"main"}'::jsonb),

    (1, 'MULTI_CHOICE', 'Which are Java access modifiers?', 3,
     '["public","static","protected","volatile"]'::jsonb,
     '{"values":["public","protected"]}'::jsonb),

    (1, 'TRUE_FALSE', 'Java supports multiple inheritance of classes.', 2,
     null,
     '{"value":false}'::jsonb),

    (1, 'SHORT_ANSWER', 'Keyword to inherit from a class?', 2,
     null,
     '{"value":"extends"}'::jsonb),

    (1, 'LIST_CHOICE', 'Which spec defines JPA?', 2,
     '["JPA","JSP","JMX"]'::jsonb,
     '{"value":"JPA"}'::jsonb),

    (1, 'FILL_BLANKS', 'Fill blanks: ____ ____ ____ main(String[] args)', 4,
     null,
     '{"values":["public","static","void"]}'::jsonb),

    (1, 'SORTING', 'Put in correct order: compile, run, package', 3,
     '["compile","run","package"]'::jsonb,
     '{"values":["compile","package","run"]}'::jsonb),

    (1, 'MATCHING', 'Match term to definition id', 4,
     '{"left":["JVM","JRE"],"right":["1","2"]}'::jsonb,
     '{"pairs":{"JVM":"1","JRE":"2"}}'::jsonb);

-- quiz 2: keep a couple of demo questions if you want (optional)
insert into questions (quiz_id, type, prompt, points, answer_key)
values
    (2, 'SHORT_ANSWER', 'What annotation marks a Spring REST controller?', 2, '{"value":"@RestController"}'::jsonb),
    (2, 'TRUE_FALSE', 'Spring Boot uses convention over configuration.', 1, '{"value":true}'::jsonb);



-- password: creator123!
insert into users (id, email, password_hash, role)
values
    (1, 'creator@example.com', '$2a$10$TQ.u8Zu5HX8QYNjyZ1lAreeV72T1cseCw2FUleg3fRUDENuLMdukG', 'CREATOR')
    on conflict (id) do update
                            set email = excluded.email,
                            password_hash = excluded.password_hash,
                            role = excluded.role;

update questions
set options = '["main","start","run"]'::jsonb,
    answer_key = '{"value":"main"}'::jsonb
where prompt = 'What is the entry point of a Java application?';


select setval(pg_get_serial_sequence('users', 'id'), (select coalesce(max(id), 1) from users));
select setval(pg_get_serial_sequence('quizzes', 'id'), (select coalesce(max(id), 1) from quizzes));
select setval(pg_get_serial_sequence('questions', 'id'), (select coalesce(max(id), 1) from questions));

