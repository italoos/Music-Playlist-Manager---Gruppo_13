package com.musicmanager;

import java.util.List;

import com.musicmanager.model.Playlist;
import com.musicmanager.model.Track;

public class PlaylistYearGenerator implements PlaylistGenerator {

    private final int year;

    public PlaylistYearGenerator(int year) {
        this.year = year;
    }

    @Override
    public Playlist generate(String playlistName, List<Track> tracks) {

        Playlist playlist = new Playlist(playlistName);

        for (Track track : tracks) {
            if (track.getYear() == year) {
                playlist.addTrack(track);
            }
        }

        return playlist;
    }
}