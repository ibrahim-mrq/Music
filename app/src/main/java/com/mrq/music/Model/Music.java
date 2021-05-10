package com.mrq.music.Model;

import android.graphics.Bitmap;
import android.net.Uri;

import java.io.Serializable;

public class Music  implements Serializable {

    public long songId;
    public String songTitle;
    public String songArtist;
    public String songDuration;
    public String songPath;
    public Uri  image;

    public Music() {
    }

    public long getSongId() {
        return songId;
    }

    public void setSongId(long songId) {
        this.songId = songId;
    }

    public String getSongTitle() {
        return songTitle;
    }

    public void setSongTitle(String songTitle) {
        this.songTitle = songTitle;
    }

    public String getSongArtist() {
        return songArtist;
    }

    public void setSongArtist(String songArtist) {
        this.songArtist = songArtist;
    }

    public String getSongDuration() {
        return songDuration;
    }

    public void setSongDuration(String songDuration) {
        this.songDuration = songDuration;
    }

    public String getSongPath() {
        return songPath;
    }

    public void setSongPath(String songPath) {
        this.songPath = songPath;
    }

    public Uri getImage() {
        return image;
    }

    public void setImage(Uri  image) {
        this.image = image;
    }
}
