package com.musicmanager;

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

    @Test
    void getFirstCreatesACompleteShuffledQueue() {
        List<Track> tracks = createTracks(1, 4);
        ShuffleStrategy strategy = new ShuffleStrategy(new Random(1));

        List<Track> playbackOrder = collectPlaybackOrder(strategy, tracks);

        assertEquals(tracks.size(), playbackOrder.size());
        assertEquals(new HashSet<>(tracks), new HashSet<>(playbackOrder));
        assertFalse(tracks.equals(playbackOrder));
    }

    @Test
    void getFirstRebuildsTheQueueForEachPlaylist() {
        List<Track> firstPlaylist = createTracks(1, 4);
        List<Track> secondPlaylist = createTracks(10, 3);
        ShuffleStrategy strategy = new ShuffleStrategy(new Random(2));

        collectPlaybackOrder(strategy, firstPlaylist);
        List<Track> secondPlaybackOrder = collectPlaybackOrder(strategy, secondPlaylist);

        assertEquals(new HashSet<>(secondPlaylist), new HashSet<>(secondPlaybackOrder));
    }

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

    private List<Track> collectPlaybackOrder(ShuffleStrategy strategy, List<Track> tracks) {
        return collectPlaybackOrder(strategy, tracks, strategy.getFirst(tracks, null));
    }

    private List<Track> collectPlaybackOrder(
            ShuffleStrategy strategy, List<Track> tracks, Track firstTrack) {
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
            tracks.add(new Track(id, "Track " + id, "Artist", 180, "Genre", 2020));
        }

        return tracks;
    }
}
