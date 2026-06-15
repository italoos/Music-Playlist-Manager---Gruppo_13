package com.musicmanager.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.musicmanager.PlaybackEngine;
import com.musicmanager.model.Playlist;
import com.musicmanager.model.Track;
import com.musicmanager.repository.PlaylistRepository;
import com.musicmanager.repository.TrackRepository;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import javafx.application.Platform;
import javafx.scene.control.ListView;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Test JUnit per l'eliminazione delle playlist dal controller principale. */

class MainControllerDeletePlaylistTest {

    private static class InMemoryPlaylistRepository implements PlaylistRepository {

        private final List<Playlist> storage = new ArrayList<>();
        private Integer lastDeletedPlaylistId;

        @Override
        public void save(Playlist playlist) {
            storage.add(playlist);
        }

        @Override
        public void update(Playlist playlist) {
        }

        @Override
        public void delete(int playlistId) {
            lastDeletedPlaylistId = playlistId;
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

    }

    private static class EmptyTrackRepository implements TrackRepository {

        @Override
        public List<Track> findAll() {
            return new ArrayList<>();
        }

        @Override
        public List<Track> findAllByPlayCount() {
            return new ArrayList<>();
        }

        @Override
        public void save(Track track) {
        }

        @Override
        public void update(Track track) {
        }

        @Override
        public void delete(int id) {
        }

    }

    private MainController controller;
    private InMemoryPlaylistRepository repository;
    private PlaybackEngine playbackEngine;
    private ListView<Playlist> playlistListView;
    private Playlist playlist;
    private Track track;

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
        repository = new InMemoryPlaylistRepository();
        playbackEngine = PlaybackEngine.getInstance();
        controller = new MainController(new EmptyTrackRepository(), repository, playbackEngine);

        playlistListView = new ListView<>();
        setControllerField("playlistListView", playlistListView);

        track = new Track(1, "Bad Guy", "Billie Eilish", 194, "Pop", 2019, 0);
        playlist = new Playlist(1, "Preferiti");
        playlist.addTrack(track);

        repository.save(playlist);
        playlistListView.getItems().add(playlist);
        playbackEngine.setCurrentPlaylist(playlist);
        playbackEngine.setCurrentTrack(track);
    }

    private void setControllerField(String fieldName, Object value) throws ReflectiveOperationException {
        Field field = MainController.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(controller, value);
    }

    @Test
    void handleDeletePlaylistRemovesPlaylistAndClearsPlayback() {
        controller.handleDeletePlaylist(playlist);

        assertEquals(playlist.getId(), repository.lastDeletedPlaylistId);
        assertNull(repository.findById(playlist.getId()));
        assertFalse(playlistListView.getItems().contains(playlist));
        assertNull(playbackEngine.getCurrentPlaylist());
        assertNull(playbackEngine.getCurrentTrack());
    }

    @Test
    void handleDeletePlaylistUsesSelectedPlaylistWhenArgumentIsNull() {
        playlistListView.getSelectionModel().select(playlist);

        controller.handleDeletePlaylist(null);

        assertEquals(playlist.getId(), repository.lastDeletedPlaylistId);
        assertFalse(playlistListView.getItems().contains(playlist));
    }

    @Test
    void handleDeletePlaylistWithoutTargetDoesNothing() {
        playlistListView.getSelectionModel().clearSelection();

        controller.handleDeletePlaylist(null);

        assertNull(repository.lastDeletedPlaylistId);
        assertEquals(1, repository.findAll().size());
        assertEquals(1, playlistListView.getItems().size());
        assertEquals(playlist, playbackEngine.getCurrentPlaylist());
        assertEquals(track, playbackEngine.getCurrentTrack());
    }

}
