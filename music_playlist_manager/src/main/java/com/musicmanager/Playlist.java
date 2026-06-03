package com.musicmanager;
import java.util.ArrayList;
import java.util.List;

public class Playlist {

    private String name;
    private List<Track> tracks;

    public Playlist(String name) {
        this.name = name;
        this.tracks = new ArrayList<>();
    }

    public void addTrack(Track t) {
        tracks.add(t);
    }

    public List<Track> getTracks() {
        return tracks;
    }

    public String getName() {
        return name;
    }
}