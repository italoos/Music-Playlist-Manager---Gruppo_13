package com.musicmanager;

import java.util.List;
import java.util.Random;
import com.musicmanager.model.Track;

public class ShuffleStrategy implements PlaybackStrategy {

    private final Random random = new Random();

    /**
     * Restituisce un brano casuale dalla lista dei brani.
     * @param tracks La lista dei brani da cui scegliere il brano successivo.
     * @param currentIndex L'indice del brano corrente nella lista dei brani.
     * @return Un brano casuale dalla lista dei brani.
     */
    @Override
    public Track getNext(List<Track> tracks, int currentIndex) {

        if(tracks.isEmpty()) {
            return null;
        }

        return tracks.get(
            random.nextInt(tracks.size())
        );
    }
}