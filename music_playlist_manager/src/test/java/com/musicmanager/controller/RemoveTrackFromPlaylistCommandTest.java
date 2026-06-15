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

/** Test JUnit per la rimozione di un brano musicale da una playlist. */

class RemoveTrackFromPlaylistCommandTest {

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
        playlist = new Playlist(1, "Preferiti");
        track = new Track(1, "Bad Guy", "Billie Eilish", 194, "Pop", 2019, 0);

        playlist.addTrack(track);
        repository.save(playlist);

    }

    /** Verifica che la rimozione di una traccia da una playlist aggiorni la playlist e il repository. */

    @Test
    void executeRemovesTrackFromPlaylistAndUpdatesRepository() {

        RemoveTrackFromPlaylistCommand command = new RemoveTrackFromPlaylistCommand(playlist, track, repository);

        command.execute();

        assertFalse(playlist.getTracks().contains(track));
        assertEquals(playlist, repository.findById(playlist.getId()));
        assertEquals(playlist, repository.lastUpdatedPlaylist);

    }

    /** Verifica che la rimozione di una traccia non ne modifichi i dati, ma solo la sua presenza nella playlist. */

    @Test
    void executeKeepsTrackAvailableOutsidePlaylist() {

        RemoveTrackFromPlaylistCommand command = new RemoveTrackFromPlaylistCommand(playlist, track, repository);

        command.execute();

        assertEquals(1, track.getId());
        assertEquals("Bad Guy", track.getTitle());
        assertEquals("Billie Eilish", track.getAuthor());

    }

    /** Verifica che l'annullamento della rimozione di una traccia ripristini la traccia nella playlist e aggiorni il repository. */

    @Test
    void undoAddsTrackBackToPlaylistAndUpdatesRepository() {

        RemoveTrackFromPlaylistCommand command = new RemoveTrackFromPlaylistCommand(playlist, track, repository);

        command.execute();
        command.undo();

        assertTrue(playlist.getTracks().contains(track));
        assertEquals(playlist, repository.lastUpdatedPlaylist);

    }

    /** Verifica che l'undo del comando ripristini la traccia alla posizione originale nella playlist. */

    @Test
    void undoRestoresTrackToItsExactOriginalIndexInPlaylist() {

        Track trackPrima = new Track(10, "Traccia Prima", "Autore A", 180, "Pop", 2020, 0);
        Track trackDopo = new Track(12, "Traccia Dopo", "Autore B", 200, "Pop", 2021, 0);

        playlist.getTracks().add(0, trackPrima);
        playlist.getTracks().add(trackDopo);

        RemoveTrackFromPlaylistCommand command = new RemoveTrackFromPlaylistCommand(playlist, track, repository);
        command.execute();

        assertFalse(playlist.getTracks().contains(track));

        command.undo();

        assertEquals(1, playlist.getTracks().indexOf(track), "La traccia deve essere ripristinata esattamente alla sua posizione originale all'interno della playlist.");

    }

}