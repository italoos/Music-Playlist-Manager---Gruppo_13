package com.musicmanager.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.musicmanager.model.Playlist;
import com.musicmanager.repository.PlaylistRepository;

import javafx.application.Platform;

/** Test JUnit per verificare la creazione e il salvataggio di una playlist. */
class CreatePlaylistTest {

    /** Implementazione in-memory di PlaylistRepository usata per simulare la persistenza delle playlist durante i test. */
    private static class InMemoryPlaylistRepository implements PlaylistRepository {

        private final List<Playlist> storage = new ArrayList<>();

        @Override
        public void save(Playlist playlist) {
            storage.add(playlist);
        }

        @Override
        public void update(Playlist playlist) {
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
    }

    private InMemoryPlaylistRepository repository;
    private Playlist playlist;

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

    /** Inizializza il repository in-memory prima di ciascun test. */
    @BeforeEach
    void setUp() {
        repository = new InMemoryPlaylistRepository();
    }

    /** Verifica che il salvataggio di una playlist inserisca correttamente l'oggetto nel repository. */
    @Test
    void saveAddsPlaylistToRepositoryCorrectly() {
        playlist = new Playlist(2, "Rock Classics");

        repository.save(playlist);

        // Verifica che la dimensione del database simulato sia ora pari ad 1
        assertEquals(1, repository.findAll().size());
        // Verifica che l'oggetto sia recuperabile tramite il suo ID e mantenga le proprietà
        assertEquals(playlist, repository.findById(2));
        assertEquals("Rock Classics", repository.findById(2).getName());
    }
}