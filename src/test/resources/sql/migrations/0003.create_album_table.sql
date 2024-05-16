create table album
(
    id           INTEGER PRIMARY KEY,
    date_created TIMESTAMP,
    date_updated TIMESTAMP,
    name         VARCHAR(128),
    arts         varchar(1024)
);