package org.example;

import java.io.Serial;
import java.io.Serializable;

public class Song implements Serializable {
    @Serial
    private static final long serialVersionUID = 2972468634978564409L;

    private String id;
    private String title;
    private String artist;
    private String filePath;
    private byte[] picture;
    public Song(String id, String title, String artist, String filePath, byte[] picture) {
        this.id = id;
        this.title = title;
        this.artist = artist;
        this.filePath = filePath;
        this.picture = picture;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getArtist() {
        return artist;
    }

    public String getFilePath() {
        return filePath;
    }

    public byte[] getPicture() {
        return picture;
    }
}