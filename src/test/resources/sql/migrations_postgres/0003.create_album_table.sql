create table album
(
    id           SERIAL PRIMARY KEY,
    date_created TIMESTAMP,
    date_updated TIMESTAMP,
    name         VARCHAR(128),
    arts         jsonb
);