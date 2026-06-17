package com.musicmanager.model.generator;

import java.util.List;

import com.musicmanager.model.Playlist;
import com.musicmanager.model.Track;

/**
 * Generatore che crea playlist contenenti solo tracce pubblicate in un anno specifico.
 */
public class PlaylistYearGenerator implements PlaylistGenerator {

    private final int year;

    /**
     * Crea un generatore basato sull'anno di pubblicazione.
     * @param year Anno da usare come criterio di filtro.
     */
    public PlaylistYearGenerator(int year) {
        this.year = year;
    }

    /** {@inheritDoc} */
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
