package com.musicmanager.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

import javafx.collections.ObservableList;
import javafx.application.Platform;
import javafx.scene.control.ListView;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Test JUnit per la modifica dei metadati di un brano musicale. */

class MainControllerUpdateTrackTest {

    /** Implementazione semplificata di PlaylistRepository usata per soddisfare le dipendenze del controller durante i test. */

    private static class InMemoryPlaylistRepository implements PlaylistRepository {

        @Override
        public void save(Playlist playlist) {
        }

        @Override
        public void update(Playlist playlist) {
        }

        @Override
        public void delete(int playlistId) {
        }

        @Override
        public Playlist findById(int playlistId) {
            return null;
        }

        @Override
        public List<Playlist> findAll() {
            return new ArrayList<>();
        }

        @Override
        public List<Playlist> findAllByPlayCount() {
            return new ArrayList<>();
        }

    }

    /** Implementazione in-memory di TrackRepository usata per simulare la persistenza delle tracce durante i test. */

    private static class InMemoryTrackRepository implements TrackRepository {

        private final List<Track> storage = new ArrayList<>();
        private Track lastUpdatedTrack;

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
            lastUpdatedTrack = track;
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
    private InMemoryTrackRepository repository;
    private Track originalTrack;

    /** Inizializza JavaFX per consentire l'uso dei componenti UI nei test. */

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

    /** Inizializza i repository e i dati necessari ai diversi scenari dei test. */

    @BeforeEach
    void setUp() throws ReflectiveOperationException {

        repository = new InMemoryTrackRepository();
        controller = new MainController(repository, new InMemoryPlaylistRepository(), PlaybackEngine.getInstance());
        setControllerField("playlistListView", new ListView<Playlist>());
        setControllerField("playlistTracksListView", new ListView<Track>());

        originalTrack = new Track(1, "Bad Guy", "Billie Eilish", 194, "Pop", 2019, 3);
        repository.save(originalTrack);
        controller.getTracks().add(originalTrack);

    }

    /** Modifica un campo privato del controller forzandone l’accesso. */

    private void setControllerField(String fieldName, Object value) throws ReflectiveOperationException {
        Field field = MainController.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(controller, value);
    }

    /** Verifica che l'aggiornamento di una traccia modifichi i metadati, il catalogo delle tracce e il repository. */

    @Test
    void handleUpdateTrackUpdatesTrackFieldsListAndRepository() {

        Track updatedTrack = new Track(1, "Ocean Eyes", "Billie Eilish", 180, "Alternative", 2016, 0);

        controller.handleUpdateTrack(originalTrack, updatedTrack);

        assertEquals("Ocean Eyes", originalTrack.getTitle());
        assertEquals("Billie Eilish", originalTrack.getAuthor());
        assertEquals(180, originalTrack.getLength());
        assertEquals("Alternative", originalTrack.getGenre());
        assertEquals(2016, originalTrack.getYear());
        assertEquals(0, originalTrack.getPlayCount());

        ObservableList<Track> tracks = controller.getTracks();
        assertEquals(1, tracks.size());
        assertEquals(originalTrack, tracks.get(0));
        assertEquals(originalTrack, repository.findAll().get(0));
        assertEquals(originalTrack, repository.lastUpdatedTrack);

    }

    /** Verifica che input non validi non modifichino né la traccia né il repository. */

    @Test
    void handleUpdateTrackUpdatesTrackCopiesInsidePlaylists() throws ReflectiveOperationException {

        Track playlistTrack = new Track(1, "Bad Guy", "Billie Eilish", 194, "Pop", 2019, 3);
        Playlist playlist = new Playlist(1, "Preferiti", 0);
        playlist.addTrack(playlistTrack);
        controller.getPlaylists().add(playlist);
        setControllerField("selectedPlaylist", playlist);

        Track updatedTrack = new Track(1, "Ocean Eyes", "Billie Eilish", 180, "Alternative", 2016, 0);

        controller.handleUpdateTrack(originalTrack, updatedTrack);

        assertEquals("Ocean Eyes", playlistTrack.getTitle());
        assertEquals("Billie Eilish", playlistTrack.getAuthor());
        assertEquals(180, playlistTrack.getLength());
        assertEquals("Alternative", playlistTrack.getGenre());
        assertEquals(2016, playlistTrack.getYear());
        assertEquals(0, playlistTrack.getPlayCount());

    }

    @Test
    void handleUpdateTrackWithInvalidInputDoesNotChangeTrackOrRepository() {

        controller.handleUpdateTrack(originalTrack, null);
        controller.handleUpdateTrack(null, new Track(1, "Ocean Eyes", "Billie Eilish", 180, "Alternative", 2016, 0));

        assertEquals("Bad Guy", originalTrack.getTitle());
        assertEquals("Billie Eilish", originalTrack.getAuthor());
        assertEquals(194, originalTrack.getLength());
        assertEquals("Pop", originalTrack.getGenre());
        assertEquals(2019, originalTrack.getYear());
        assertEquals(3, originalTrack.getPlayCount());
        assertNull(repository.lastUpdatedTrack);

    }

    @Test
    void updateSyncsPlayCountAcrossTrackCopiesWhenPlaybackStartsFromPlaylist() {
        Track playlistTrack = new Track(1, "Bad Guy", "Billie Eilish", 194, "Pop", 2019, 0);
        Playlist playlist = new Playlist(1, "Preferiti", 0);
        playlist.addTrack(playlistTrack);

        controller.getPlaylists().add(playlist);
        controller.update(playlistTrack, playlist, 0, false);

        assertEquals(3, originalTrack.getPlayCount());
        assertEquals(0, playlist.getTracks().get(0).getPlayCount());
    }

}
