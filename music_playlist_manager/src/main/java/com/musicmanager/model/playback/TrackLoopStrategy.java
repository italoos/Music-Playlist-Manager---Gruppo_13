package com.musicmanager.model.playback;

import java.util.List;
import com.musicmanager.model.Track;

/**
 * Strategia che ripete sempre la traccia corrente.
 */
public class TrackLoopStrategy implements PlaybackStrategy {

    /**
     * Implementa la selezione del brano corrente nella lista dei brani come prossimo brano da riprodurre.
     * @param tracks La lista dei brani da cui ottenere il brano successivo.
     * @param currentIndex L'indice del brano corrente nella lista dei brani.
     * @return Il brano corrente.
     */
    @Override
    public Track getNext(List<Track> tracks, int currentIndex) {
        
        if (tracks.isEmpty()) return null;

        return tracks.get(currentIndex);
    }
}
