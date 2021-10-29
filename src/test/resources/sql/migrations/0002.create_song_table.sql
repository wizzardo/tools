create table song
(
    id           INTEGER PRIMARY KEY,
    date_created TIMESTAMP,
    date_updated TIMESTAMP,
    name         VARCHAR(128),
    artist_id    INTEGER,
    genre        VARCHAR(128),
    CONSTRAINT "fk_song_artist" FOREIGN KEY (artist_id) REFERENCES artist (id) ON DELETE CASCADE ON UPDATE CASCADE
);