package com.wizzardo.tools.sql.model;

import java.util.Date;
import java.util.List;

public class Album {
    public long id;
    public Date dateCreated;
    public Date dateUpdated;
    public String name;
    public List<AlbumArt> arts;

    public Album() {
    }

    public Album(long id, Date dateCreated, Date dateUpdated, String name, List<AlbumArt> arts) {
        this.id = id;
        this.dateCreated = dateCreated;
        this.dateUpdated = dateUpdated;
        this.name = name;
        this.arts = arts;
    }

    public static class AlbumArt {
        public String url;


        public AlbumArt() {
        }

        public AlbumArt(String url) {
            this.url = url;
        }
    }
}
