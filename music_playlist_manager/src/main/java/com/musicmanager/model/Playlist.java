package com.musicmanager.model;

import java.util.ArrayList;
import java.util.List;

public class Playlist {

    private String name;
    private int playCount;
    private List<Track> tracks;
    private int id;

    public Playlist(String name) {
        this.name = name;
        this.playCount = 0;
        this.tracks = new ArrayList<>();
    }

    public Playlist(int id, String name, int playCount) {
        this.id = id;
        this.name = name;
        this.playCount = playCount;
        this.tracks = new ArrayList<>();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void addTrack(Track track) {
        if (!tracks.contains(track)) {
            tracks.add(track);
        }
    }

    public void removeTrack(Track track) {
        tracks.remove(track);
    }

    public List<Track> getTracks() {
        return tracks;
    }

    public int indexOf(Track track) {
        return tracks.indexOf(track);
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPlayCount() {
        return this.playCount;
    }

    public void incrementPlayCount() {
        this.playCount ++;
    }
}