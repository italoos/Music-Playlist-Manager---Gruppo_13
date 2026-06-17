package com.musicmanager.model.playback;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

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

    private final Random random;
    private List<Track> shuffledTracks = new ArrayList<>();

    public ShuffleStrategy() {
        this(new Random());
    }

    ShuffleStrategy(Random random) {
        this.random = random;
    }

    @Override
    public Track getFirst(List<Track> tracks, Track preferredTrack) {
        shuffledTracks = new ArrayList<>(tracks);
        Collections.shuffle(shuffledTracks, random);

        if (shuffledTracks.isEmpty()) {
            return null;
        }

        if (preferredTrack != null && shuffledTracks.remove(preferredTrack)) {
            shuffledTracks.add(0, preferredTrack);
        }

        return shuffledTracks.get(0);
    }

    /**
     * Restituisce il brano successivo nella coda casuale corrente.
     * @param tracks La lista dei brani della playlist corrente.
     * @param currentIndex L'indice del brano corrente nella playlist originale.
     * @return Il brano successivo nella coda casuale, oppure null se terminata.
     */
    @Override
    public Track getNext(List<Track> tracks, int currentIndex) {
        if (tracks.isEmpty() || currentIndex < 0 || currentIndex >= tracks.size()) {
            return null;
        }

        if (shuffledTracks.isEmpty() || !shuffledTracks.containsAll(tracks)
                || shuffledTracks.size() != tracks.size()) {
            getFirst(tracks, tracks.get(currentIndex));
        }

        int shuffledIndex = shuffledTracks.indexOf(tracks.get(currentIndex));

        if (shuffledIndex + 1 < shuffledTracks.size()) {
            return shuffledTracks.get(shuffledIndex + 1);
        }

        return null;
    }
}
