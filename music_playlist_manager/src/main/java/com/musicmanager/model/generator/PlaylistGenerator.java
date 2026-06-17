package com.musicmanager.model.generator;

import java.util.List;

import com.musicmanager.model.Playlist;
import com.musicmanager.model.Track;

/**
 * Contratto per le strategie di generazione automatica delle playlist.
 */
public interface PlaylistGenerator {

    /**
     * Genera una playlist filtrando un catalogo di tracce.
     * @param playlistName Nome da assegnare alla playlist generata.
     * @param tracks Catalogo di tracce da filtrare.
     * @return La playlist generata secondo il criterio della strategia concreta.
     */
    Playlist generate(String playlistName,List<Track> tracks);
}
