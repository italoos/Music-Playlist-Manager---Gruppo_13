package com.musicmanager;

import static org.junit.jupiter.api.Assertions.*;

import com.musicmanager.model.Track;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PlaybackEngineTest {

    @BeforeEach
    void resetEngine() {
        PlaybackEngine engine = PlaybackEngine.getTestInstance();
        engine.setCurrentTrack(null);
        engine.setState(new PausedState());
    }

    @Test
    void playWithTrackStartsPlaying() {
        PlaybackEngine engine = PlaybackEngine.getInstance();
        Track track = new Track(1, "Lose Yourself", "Eminem", 326, "Hip Hop", 2002);
        engine.setCurrentTrack(track);

        engine.play();

        assertTrue(engine.isPlaying());
        assertEquals(track, engine.getCurrentTrack());
    }

    @Test
    void pauseWhenPlayingTransitionsToPaused() {
        PlaybackEngine engine = PlaybackEngine.getInstance();
        Track track = new Track(2, "Billie Jean", "Michael Jackson", 293, "Pop", 1982);
        engine.setCurrentTrack(track);
        engine.play();

        assertTrue(engine.isPlaying());

        engine.pause();

        assertFalse(engine.isPlaying());
        assertEquals(track, engine.getCurrentTrack());
    }
}
