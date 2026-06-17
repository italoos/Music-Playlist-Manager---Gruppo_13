package com.musicmanager.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.musicmanager.model.Playlist;
import com.musicmanager.model.Track;
import com.musicmanager.repository.PlaylistRepository;

import javafx.application.Platform;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Test JUnit per l'aggiunta di un brano musicale a una playlist. */

class AddTrackToPlaylistCommandTest {

    /** Implementazione in-memory di PlaylistRepository usata per simulare la persistenza delle playlist durante i test. */

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
            return new ArrayList<>(storage);
        }

    }

    private InMemoryPlaylistRepository repository;
    private Playlist playlist;
    private Track track;

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

    /** Inizializza il repository e i dati necessari ai diversi scenari dei test. */

    @BeforeEach
    void setUp() {

        repository = new InMemoryPlaylistRepository();
        playlist = new Playlist(1, "Preferiti", 0);
        track = new Track(1, "Bad Guy", "Billie Eilish", 194, "Pop", 2019, 0);

        repository.save(playlist);

    }

    /** Verifica che l'aggiunta di una traccia a una playlist aggiorni la playlist e il repository. */

    @Test
    void executeAddsTrackToPlaylistAndUpdatesRepository() {

        AddTrackToPlaylistCommand command = new AddTrackToPlaylistCommand(playlist, track, repository);

        command.execute();

        assertTrue(playlist.getTracks().contains(track));
        assertEquals(playlist, repository.findById(playlist.getId()));
        assertEquals(playlist, repository.lastUpdatedPlaylist);

    }

    /** Verifica che l'aggiunta di una traccia già presente non produca duplicati nella playlist. */

    @Test
    void executeDoesNotAddDuplicateTrack() {

        playlist.addTrack(track);
        AddTrackToPlaylistCommand command = new AddTrackToPlaylistCommand(playlist, track, repository);

        command.execute();

        assertEquals(1, playlist.getTracks().size());

    }

    /** Verifica che l'annullamento dell'aggiunta di una traccia rimuova la traccia dalla playlist e aggiorni il repository. */

    @Test
    void undoRemovesTrackFromPlaylistAndUpdatesRepository() {

        AddTrackToPlaylistCommand command = new AddTrackToPlaylistCommand(playlist, track, repository);

        command.execute();
        command.undo();

        assertFalse(playlist.getTracks().contains(track));
        assertEquals(playlist, repository.lastUpdatedPlaylist);

    }

}