package com.wizzardo.tools.sql.model;

import java.sql.Timestamp;

public class Song {
    public enum Genre {
        POP, METAL, ROCK
    }

    public long id;
    public Timestamp dateCreated;
    public Timestamp dateUpdated;
    public String name;
    public Long artistId;
    public Genre genre;

    public Song() {
    }

    public Song(long id, Timestamp dateCreated, Timestamp dateUpdated, String name, long artistId, Genre genre) {
        this.id = id;
        this.dateCreated = dateCreated;
        this.dateUpdated = dateUpdated;
        this.name = name;
        this.artistId = artistId;
        this.genre = genre;
    }

    @Override
    public String toString() {
        return "Song{" +
                "id=" + id +
                ", dateCreated=" + dateCreated +
                ", dateUpdated=" + dateUpdated +
                ", name='" + name + '\'' +
                ", artistId=" + artistId +
                ", genre=" + genre +
                '}';
    }
}
