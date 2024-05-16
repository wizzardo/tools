package com.wizzardo.tools.sql.model;

public class ArtistAlbum {
    public long artistId;
    public long albumId;

    public ArtistAlbum() {
    }

    public ArtistAlbum(long artistId, long albumId) {
        this.artistId = artistId;
        this.albumId = albumId;
    }
}
