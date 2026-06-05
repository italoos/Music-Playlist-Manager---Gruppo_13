package com.musicmanager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.musicmanager.model.Track;

/**
 * Implementa la modalità di riproduzione casuale di una playlist.
 *
 * Alla prima richiesta genera una copia mescolata delle tracce
 * della playlist e ne mantiene l'ordine per tutta la sessione
 * di riproduzione. Ogni traccia viene quindi riprodotta una sola
 * volta fino al termine della coda shuffle.
 *
 * Quando viene raggiunta l'ultima traccia della sequenza casuale,
 * il metodo restituisce null per indicare la fine della riproduzione.
 */
public class ShuffleStrategy implements PlaybackStrategy {

    private List<Track> shuffledTracks = new ArrayList<>();

    @Override
    public Track getNext(List<Track> tracks, int currentIndex) {

        if (tracks.isEmpty()) {
            return null;
        }

        if (shuffledTracks.isEmpty()) {

            shuffledTracks = new ArrayList<>(tracks);

            Collections.shuffle(shuffledTracks);
        }

        int shuffledIndex =
                shuffledTracks.indexOf(
                        tracks.get(currentIndex)
                );

        if (shuffledIndex + 1 < shuffledTracks.size()) {
            return shuffledTracks.get(shuffledIndex + 1);
        }

        return null;
    }
}