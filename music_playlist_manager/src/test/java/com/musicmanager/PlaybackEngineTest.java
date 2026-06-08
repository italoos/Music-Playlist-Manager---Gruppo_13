package com.musicmanager;

import static org.junit.jupiter.api.Assertions.*;

import com.musicmanager.model.Playlist;
import com.musicmanager.model.Track;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PlaybackEngineTest {

    @BeforeEach
    void resetEngine() {
        PlaybackEngine engine = PlaybackEngine.getTestInstance();
        engine.setCurrentTrack(null);
        engine.setCurrentPlaylist(null);
        engine.setState(new PausedState());
        engine.setStrategy(new SequentialStrategy());
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

    @Test
    void skipWhilePlayingMovesToNextTrack() {
        PlaybackEngine engine = PlaybackEngine.getInstance();
        Track firstTrack = new Track(1, "First", "Artist A", 180, "Rock", 2020);
        Track secondTrack = new Track(2, "Second", "Artist B", 210, "Pop", 2021);
        Playlist playlist = new Playlist("Test playlist");
        playlist.addTrack(firstTrack);
        playlist.addTrack(secondTrack);
        engine.setCurrentPlaylist(playlist);
        engine.setCurrentTrack(firstTrack);
        engine.play();

        engine.skip();

        assertSame(secondTrack, engine.getCurrentTrack());
        assertTrue(engine.isPlaying());
    }

    @Test
    void skipWhilePausedMovesToNextTrackAndStartsPlaying() {
        PlaybackEngine engine = PlaybackEngine.getInstance();
        Track firstTrack = new Track(1, "First", "Artist A", 180, "Rock", 2020);
        Track secondTrack = new Track(2, "Second", "Artist B", 210, "Pop", 2021);
        Playlist playlist = new Playlist("Test playlist");
        playlist.addTrack(firstTrack);
        playlist.addTrack(secondTrack);
        engine.setCurrentPlaylist(playlist);
        engine.setCurrentTrack(firstTrack);

        engine.skip();

        assertSame(secondTrack, engine.getCurrentTrack());
        assertTrue(engine.isPlaying());
    }

    @Test
    void skipLastTrackStopsPlayback() {
        PlaybackEngine engine = PlaybackEngine.getInstance();
        Track track = new Track(1, "Only track", "Artist", 180, "Rock", 2020);
        Playlist playlist = new Playlist("Test playlist");
        playlist.addTrack(track);
        engine.setCurrentPlaylist(playlist);
        engine.setCurrentTrack(track);
        engine.play();

        engine.skip();

        assertNull(engine.getCurrentTrack());
        assertFalse(engine.isPlaying());
    }

    @Test
    void startPlaylistUsesStrategyToChooseTheFirstTrack() {
        PlaybackEngine engine = PlaybackEngine.getInstance();
        Track firstTrack = new Track(1, "First", "Artist A", 180, "Rock", 2020);
        Track strategyFirstTrack = new Track(2, "Strategy first", "Artist B", 210, "Pop", 2021);
        Playlist playlist = new Playlist("Test playlist");
        playlist.addTrack(firstTrack);
        playlist.addTrack(strategyFirstTrack);
        engine.setStrategy(new PlaybackStrategy() {
            @Override
            public Track getFirst(java.util.List<Track> tracks, Track preferredTrack) {
                return strategyFirstTrack;
            }

            @Override
            public Track getNext(java.util.List<Track> tracks, int currentIndex) {
                return null;
            }
        });

        engine.startPlaylist(playlist);

        assertSame(strategyFirstTrack, engine.getCurrentTrack());
        assertTrue(engine.isPlaying());
    }
}
