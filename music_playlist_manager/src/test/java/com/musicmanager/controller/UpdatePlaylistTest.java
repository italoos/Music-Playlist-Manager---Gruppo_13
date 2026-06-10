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

/** Test JUnit per verificare la modifica e la rinominazione di una playlist esistente. */
class UpdatePlaylistTest {

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

    /** Inizializza il repository e pre-carica una playlist da modificare nel corso del test. */
    @BeforeEach
    void setUp() {
        repository = new InMemoryPlaylistRepository();
        playlist = new Playlist(1, "Preferiti");
        repository.save(playlist); // La playlist iniziale è registrata nel database simulato
    }

    /** Verifica che la modifica del nome aggiorni correttamente l'entità all'interno del repository. */
    @Test
    void updateChangesPlaylistNameInRepository() {
        // Applica la modifica del nome (Rinominazione)
        playlist.setName("Musica Pop 2026");

        // Aggiorna lo stato nel repository simulato
        repository.update(playlist);

        // Controlla se l'oggetto memorizzato riflette il nuovo nome assegnato
        assertEquals("Musica Pop 2026", repository.findById(1).getName());
        // Controlla se l'ultimo oggetto aggiornato coincide con la playlist modificata
        assertEquals(playlist, repository.lastUpdatedPlaylist);
    }
}