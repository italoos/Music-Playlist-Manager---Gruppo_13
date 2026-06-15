package com.musicmanager;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.musicmanager.model.Track;

class TrackLoopStrategyTest {

    private TrackLoopStrategy strategy;
    private Track firstTrack;
    private Track secondTrack;
    private Track thirdTrack;
    private List<Track> tracks;

    @BeforeEach
    void setUp() {
        strategy = new TrackLoopStrategy();
        firstTrack = new Track(1, "First", "Artist A", 180, "Rock", 2020, 0);
        secondTrack = new Track(2, "Second", "Artist B", 210, "Pop", 2021, 0);
        thirdTrack = new Track(3, "Third", "Artist C", 240, "Jazz", 2022, 0);
        tracks = List.of(firstTrack, secondTrack, thirdTrack);
    }

    @Test
    void getNextWithEmptyTrackListReturnsNull() {
        Track nextTrack = strategy.getNext(List.of(), 0);

        assertNull(nextTrack);
    }

    @Test
    void getNextFromFirstTrackReturnsFirstTrack() {
        Track nextTrack = strategy.getNext(tracks, 0);

        assertSame(firstTrack, nextTrack);
    }

    @Test
    void getNextFromMiddleTrackReturnsMiddleTrack() {
        Track nextTrack = strategy.getNext(tracks, 1);

        assertSame(secondTrack, nextTrack);
    }

    @Test
    void getNextFromLastTrackReturnsLastTrack() {
        Track nextTrack = strategy.getNext(tracks, tracks.size() - 1);

        assertSame(thirdTrack, nextTrack);
    }
}
