package com.wizzardo.tools.sql.model;

import java.sql.Timestamp;

public class Artist {
    public long id;
    public Timestamp dateCreated;
    public Timestamp dateUpdated;
    public String name;

    public Artist() {
    }

    public Artist(long id, Timestamp dateCreated, Timestamp dateUpdated, String name) {
        this.id = id;
        this.dateCreated = dateCreated;
        this.dateUpdated = dateUpdated;
        this.name = name;
    }

    @Override
    public String toString() {
        return "Artist{" +
                "id=" + id +
                ", dateCreated=" + dateCreated +
                ", dateUpdated=" + dateUpdated +
                ", name='" + name + '\'' +
                '}';
    }
}
