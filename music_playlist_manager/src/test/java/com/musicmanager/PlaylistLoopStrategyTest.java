package com.musicmanager;

import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.musicmanager.model.Track;

class PlaylistLoopStrategyTest {

    /**
     * Il seguente test controlla che:
     * - la riproduzione segua l'ordine della playlist
     * - ogni traccia venga seguita da quella immediatamente successiva
     */
    @Test
    void returnsNextTrackInPlaylistOrder() {

        List<Track> tracks = createTracks(1, 3);

        PlaylistLoopStrategy strategy = new PlaylistLoopStrategy();

        assertSame(tracks.get(1), strategy.getNext(tracks, 0));

        assertSame(tracks.get(2), strategy.getNext(tracks, 1));
    }

    /**
     * Il seguente test controlla che:
     * - al termine dell'ultima traccia la riproduzione non si interrompa
     * - la strategia ritorni alla prima traccia della playlist
     * - la playlist venga quindi riprodotta ciclicamente
     */
    @Test
    void returnsFirstTrackAfterLastTrack() {

        List<Track> tracks = createTracks(1, 3);

        PlaylistLoopStrategy strategy = new PlaylistLoopStrategy();

        assertSame(tracks.get(0), strategy.getNext(tracks, 2));
    }

    private List<Track> createTracks(int firstId, int count) {

        List<Track> tracks = new ArrayList<>();

        for (int i = 0; i < count; i++) {

            int id = firstId + i;
            tracks.add(new Track(id, "Track " + id, "Artist",180, "Genre", 2020, 0));
        }

        return tracks;
    }
}