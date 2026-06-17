package com.musicmanager.model.playback;

import java.util.List;
import com.musicmanager.model.Track;

/**
 * Strategia usata dal PlaybackEngine per decidere l'ordine di riproduzione.
 */
public interface PlaybackStrategy {

    /**
     * Restituisce la prima traccia da riprodurre quando parte una nuova coda.
     * @param tracks Tracce disponibili nella playlist.
     * @param preferredTrack Traccia preferita, se gia presente nella playlist.
     * @return La prima traccia da riprodurre, oppure null se la lista e vuota.
     */
    default Track getFirst(List<Track> tracks, Track preferredTrack) {
        if (tracks.isEmpty()) {
            return null;
        }

        if (preferredTrack != null && tracks.contains(preferredTrack)) {
            return preferredTrack;
        }

        return tracks.get(0);
    }

    /**
     * Restituisce la prossima traccia secondo la strategia concreta.
     * @param tracks Tracce disponibili nella playlist.
     * @param currentIndex Indice della traccia corrente nella playlist.
     * @return La prossima traccia, oppure null se la riproduzione deve terminare.
     */
    Track getNext(List<Track> tracks, int currentIndex);

}
