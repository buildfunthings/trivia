CREATE TABLE game (
       id    SERIAL,
       date_started timestamp not null default NOW(),     
       date_completed timestamp,
       PRIMARY KEY (id)
);
