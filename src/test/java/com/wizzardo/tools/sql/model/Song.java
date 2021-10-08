package com.wizzardo.tools.sql.model;

import java.sql.Timestamp;

public class Song {
    public long id;
    public Timestamp dateCreated;
    public Timestamp dateUpdated;
    public String name;
    public Long artistId;

    public Song() {
    }

    public Song(long id, Timestamp dateCreated, Timestamp dateUpdated, String name, long artistId) {
        this.id = id;
        this.dateCreated = dateCreated;
        this.dateUpdated = dateUpdated;
        this.name = name;
        this.artistId = artistId;
    }

    @Override
    public String toString() {
        return "Song{" +
                "id=" + id +
                ", dateCreated=" + dateCreated +
                ", dateUpdated=" + dateUpdated +
                ", name='" + name + '\'' +
                ", artistId=" + artistId +
                '}';
    }
}
