create table artist
(
    id           SERIAL PRIMARY KEY,
    date_created TIMESTAMP,
    date_updated TIMESTAMP,
    name         VARCHAR(128)
);
