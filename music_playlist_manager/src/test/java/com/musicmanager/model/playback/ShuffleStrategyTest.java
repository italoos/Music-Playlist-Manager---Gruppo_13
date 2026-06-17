package com.musicmanager.model.playback;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.musicmanager.model.Track;

class ShuffleStrategyTest {

    /**
     * Il seguente test controlla che:
     * - tutte le tracce vengano riprodotte
     * - nessuna traccia venga persa
     * - l'ordine non sia quello originale
     */
    @Test
    void getFirstCreatesACompleteShuffledQueue() {
        List<Track> tracks = createTracks(1, 4);
        ShuffleStrategy strategy = new ShuffleStrategy(new Random(1));

        List<Track> playbackOrder = collectPlaybackOrder(strategy, tracks);

        assertEquals(tracks.size(), playbackOrder.size());
        assertEquals(new HashSet<>(tracks), new HashSet<>(playbackOrder));
        assertFalse(tracks.equals(playbackOrder));
    }

    /**
     * Il seguente test verifica che una playlist precedente
     * non influenzi la successiva, aggiungendo ad esempio le 
     * proprie tracce alla riproduzione della successiva
     */
    @Test
    void getFirstRebuildsTheQueueForEachPlaylist() {
        List<Track> firstPlaylist = createTracks(1, 4);
        List<Track> secondPlaylist = createTracks(10, 3);
        ShuffleStrategy strategy = new ShuffleStrategy(new Random(2));

        collectPlaybackOrder(strategy, firstPlaylist);
        List<Track> secondPlaybackOrder = collectPlaybackOrder(strategy, secondPlaylist);

        assertEquals(new HashSet<>(secondPlaylist), new HashSet<>(secondPlaybackOrder));
    }

    /**
     * Il seguente test verifica che la traccia preferita venga
     * riprodotta come prima traccia della playlist senza alterare
     * la correttezza della coda shuffle, che deve continuare a
     * contenere tutte le tracce della playlist.
     */
    @Test
    void preferredTrackIsPlayedFirstWithoutLeavingTheShuffledQueue() {
        List<Track> tracks = createTracks(1, 4);
        Track preferredTrack = tracks.get(2);
        ShuffleStrategy strategy = new ShuffleStrategy(new Random(3));

        Track firstTrack = strategy.getFirst(tracks, preferredTrack);
        List<Track> playbackOrder = collectPlaybackOrder(strategy, tracks, firstTrack);

        assertSame(preferredTrack, firstTrack);
        assertEquals(new HashSet<>(tracks), new HashSet<>(playbackOrder));
    }

    /**
     * Il seguente test controlla che al termine dell'ultima traccia 
     * nella coda di riproduzione, la riproduzione si interrompe
     */
    @Test
    void returnsNullAfterLastTrackOfShuffledQueue() {
    List<Track> tracks = createTracks(1, 3);
    ShuffleStrategy strategy = new ShuffleStrategy(new Random(1));

    List<Track> order = collectPlaybackOrder(strategy, tracks);

    Track lastTrack = order.get(order.size() - 1);
    Track next = strategy.getNext(tracks, tracks.indexOf(lastTrack));

    assertEquals(null, next);
}

    private List<Track> collectPlaybackOrder(ShuffleStrategy strategy, List<Track> tracks) {
        return collectPlaybackOrder(strategy, tracks, strategy.getFirst(tracks, null));
    }

    private List<Track> collectPlaybackOrder(ShuffleStrategy strategy, List<Track> tracks, Track firstTrack) {
        List<Track> playbackOrder = new ArrayList<>();
        Track currentTrack = firstTrack;

        while (currentTrack != null) {
            playbackOrder.add(currentTrack);
            currentTrack = strategy.getNext(tracks, tracks.indexOf(currentTrack));
        }

        return playbackOrder;
    }

    private List<Track> createTracks(int firstId, int count) {
        List<Track> tracks = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            int id = firstId + i;
            tracks.add(new Track(id, "Track " + id, "Artist", 180, "Genre", 2020, 0));
        }

        return tracks;
    }
}
