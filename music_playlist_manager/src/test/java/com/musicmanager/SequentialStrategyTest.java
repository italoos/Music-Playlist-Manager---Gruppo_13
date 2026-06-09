package com.musicmanager;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.musicmanager.model.Track;

class SequentialStrategyTest {

    /**
     * Il seguente test controlla che:
     * - la riproduzione segua l'ordine della playlist
     * - ogni traccia venga seguita da quella immediatamente successiva
     */
    @Test
    void returnsNextTrackInPlaylistOrder() {

        List<Track> tracks = createTracks(1, 3);

        SequentialStrategy strategy = new SequentialStrategy();

        assertSame(tracks.get(1), strategy.getNext(tracks, 0));

        assertSame(tracks.get(2), strategy.getNext(tracks, 1));
    }

    /**
     * Il seguente test controlla che:
     * - al termine dell'ultima traccia non esistano altre tracce da riprodurre
     * - la strategia restituisca null per indicare la fine della playlist
     */
    @Test
    void returnsNullAfterLastTrack() {

        List<Track> tracks = createTracks(1, 3);

        SequentialStrategy strategy = new SequentialStrategy();

        assertNull(strategy.getNext(tracks, 2));
    }

    private List<Track> createTracks(int firstId, int count) {

        List<Track> tracks = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            int id = firstId + i;
            tracks.add(new Track(id,"Track " + id,"Artist",180,"Genre",2020));
        }

        return tracks;
    }
}