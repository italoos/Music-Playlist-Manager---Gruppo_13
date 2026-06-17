package com.musicmanager.model.playback;

import java.util.List;
import com.musicmanager.model.Track;

/**
 * Strategia che ripete l'intera playlist tornando alla prima traccia dopo l'ultima.
 */
public class PlaylistLoopStrategy implements PlaybackStrategy {

    /**
     * Restituisce il brano successivo nella lista dei brani. Se il brano corrente è l'ultimo della lista, restituisce il primo brano.
     * @param tracks La lista dei brani da cui ottenere il brano successivo.
     * @param currentIndex L'indice del brano corrente nella lista dei brani.
     * @return Il brano successivo nella lista dei brani, o il primo brano se il brano corrente è l'ultimo della lista.
     */
    @Override
    public Track getNext(List<Track> tracks, int currentIndex) {

        if(currentIndex + 1 < tracks.size()) {
            return tracks.get(currentIndex + 1);
        }

        return tracks.get(0);
    }
}
