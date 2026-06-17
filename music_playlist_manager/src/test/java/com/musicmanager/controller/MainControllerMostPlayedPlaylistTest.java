package com.musicmanager.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import com.musicmanager.model.Playlist;
import com.musicmanager.model.Track;
import com.musicmanager.model.playback.PausedState;
import com.musicmanager.model.playback.PlaybackEngine;
import com.musicmanager.model.playback.SequentialStrategy;
import com.musicmanager.repository.PlaylistRepository;
import com.musicmanager.repository.TrackRepository;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import javafx.application.Platform;
import javafx.scene.control.ListView;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MainControllerMostPlayedPlaylistTest {

    private static class InMemoryPlaylistRepository implements PlaylistRepository {

        private final List<Playlist> storage = new ArrayList<>();
        private Playlist lastUpdatedPlaylist;

        @Override
        public void save(Playlist playlist) {
            storage.add(playlist);
        }

        @Override
        public void update(Playlist playlist) {
            lastUpdatedPlaylist = playlist;

            for (int i = 0; i < storage.size(); i++) {
                if (storage.get(i).getId() == playlist.getId()) {
                    storage.set(i, playlist);
                    return;
                }
            }
        }

        @Override
        public void delete(int playlistId) {
            storage.removeIf(playlist -> playlist.getId() == playlistId);
        }

        @Override
        public Playlist findById(int playlistId) {
            return storage.stream()
                .filter(playlist -> playlist.getId() == playlistId)
                .findFirst()
                .orElse(null);
        }

        @Override
        public List<Playlist> findAll() {
            return new ArrayList<>(storage);
        }

        @Override
        public List<Playlist> findAllByPlayCount() {
            return storage.stream()
                .sorted(Comparator.<Playlist>comparingInt(Playlist::getPlayCount).reversed()
                    .thenComparingInt(Playlist::getId))
                .limit(10)
                .toList();
        }
    }

    private static class InMemoryTrackRepository implements TrackRepository {

        private final List<Track> storage = new ArrayList<>();

        @Override
        public List<Track> findAll() {
            return new ArrayList<>(storage);
        }

        @Override
        public List<Track> findAllByPlayCount() {
            return new ArrayList<>(storage);
        }

        @Override
        public void save(Track track) {
            storage.add(track);
        }

        @Override
        public void update(Track track) {
            for (int i = 0; i < storage.size(); i++) {
                if (storage.get(i).getId() == track.getId()) {
                    storage.set(i, track);
                    return;
                }
            }
        }

        @Override
        public void delete(int id) {
            storage.removeIf(track -> track.getId() == id);
        }
    }

    private MainController controller;
    private InMemoryPlaylistRepository playlistRepository;
    private ListView<Playlist> mostPlayedPlaylistsListView;
    private Playlist chillPlaylist;
    private Playlist workoutPlaylist;
    private Playlist focusPlaylist;

    @BeforeAll
    static void startJavaFx() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        try {
            Platform.startup(latch::countDown);
        } catch (IllegalStateException e) {
            latch.countDown();
        }

        latch.await();
    }

    @BeforeEach
    void setUp() throws ReflectiveOperationException {
        PlaybackEngine playbackEngine = PlaybackEngine.getInstance();
        playbackEngine.setCurrentTrack(null);
        playbackEngine.setCurrentPlaylist(null);
        playbackEngine.setState(new PausedState());
        playbackEngine.setStrategy(new SequentialStrategy());

        Track track = new Track(1, "Bad Guy", "Billie Eilish", 194, "Pop", 2019, 0);
        InMemoryTrackRepository trackRepository = new InMemoryTrackRepository();
        trackRepository.save(track);

        playlistRepository = new InMemoryPlaylistRepository();
        chillPlaylist = playlistWithTrack(1, "Chill", 1, track);
        workoutPlaylist = playlistWithTrack(2, "Workout", 3, track);
        focusPlaylist = playlistWithTrack(3, "Focus", 2, track);
        playlistRepository.save(chillPlaylist);
        playlistRepository.save(workoutPlaylist);
        playlistRepository.save(focusPlaylist);

        controller = new MainController(trackRepository, playlistRepository, playbackEngine);
        controller.getTracks().add(track);
        controller.getPlaylists().setAll(playlistRepository.findAll());

        setControllerField("mostPlayedTracksListView", new ListView<Track>());
        mostPlayedPlaylistsListView = new ListView<>();
        setControllerField("mostPlayedPlaylistsListView", mostPlayedPlaylistsListView);
        setControllerField("playlistListView", new ListView<Playlist>());

        invokeNoArgPrivateMethod("initializeMostPlayedLists");
    }

    private Playlist playlistWithTrack(int id, String name, int playCount, Track track) {
        Playlist playlist = new Playlist(id, name, playCount);
        playlist.addTrack(track);
        return playlist;
    }

    private void setControllerField(String fieldName, Object value) throws ReflectiveOperationException {
        java.lang.reflect.Field field = MainController.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(controller, value);
    }

    private void invokeNoArgPrivateMethod(String methodName) throws ReflectiveOperationException {
        Method method = MainController.class.getDeclaredMethod(methodName);
        method.setAccessible(true);
        method.invoke(controller);
    }

    private void invokeHandlePlayPlaylist(Playlist playlist) throws ReflectiveOperationException {
        Method method = MainController.class.getDeclaredMethod("handlePlayPlaylist", Playlist.class);
        method.setAccessible(true);
        method.invoke(controller, playlist);
    }

    @Test
    void mostPlayedPlaylistSectionIsOrderedByPlayCount() {
        assertEquals(
            List.of("Workout", "Focus", "Chill"),
            visiblePlaylistNames()
        );
    }

    @Test
    void playingPlaylistUpdatesStatsAndRefreshesMostPlayedSection() throws ReflectiveOperationException {
        invokeHandlePlayPlaylist(chillPlaylist);
        invokeHandlePlayPlaylist(chillPlaylist);

        assertEquals(3, chillPlaylist.getPlayCount());
        assertSame(chillPlaylist, playlistRepository.lastUpdatedPlaylist);
        assertEquals(
            List.of("Chill", "Workout", "Focus"),
            visiblePlaylistNames()
        );
    }

    private List<String> visiblePlaylistNames() {
        return mostPlayedPlaylistsListView.getItems().stream()
            .map(Playlist::getName)
            .toList();
    }
}
