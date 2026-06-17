package com.musicmanager.model.generator;

import java.util.List;

import com.musicmanager.model.Playlist;
import com.musicmanager.model.Track;

public class PlaylistGenreGenerator implements PlaylistGenerator {

    private final String genre;

    public PlaylistGenreGenerator(String genre) {
        this.genre = genre;
    }

    @Override
    public Playlist generate(String playlistName, List<Track> tracks) {

        Playlist playlist = new Playlist(playlistName);

        for (Track track : tracks) {
            if (track.getGenre().equalsIgnoreCase(genre)) {
                playlist.addTrack(track);
            }
        }

        return playlist;
    }
}
