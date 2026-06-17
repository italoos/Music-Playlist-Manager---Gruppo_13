package com.musicmanager.model.generator;

import java.util.List;

import com.musicmanager.model.Playlist;
import com.musicmanager.model.Track;

/**
 * Generatore che crea playlist contenenti solo tracce di un determinato genere.
 */
public class PlaylistGenreGenerator implements PlaylistGenerator {

    private final String genre;

    /**
     * Crea un generatore basato sul genere musicale.
     * @param genre Genere da usare come criterio di filtro.
     */
    public PlaylistGenreGenerator(String genre) {
        this.genre = genre;
    }

    /** {@inheritDoc} */
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
