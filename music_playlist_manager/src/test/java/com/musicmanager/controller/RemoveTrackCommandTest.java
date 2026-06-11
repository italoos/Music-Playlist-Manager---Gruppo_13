package com.musicmanager.controller;

import static org.junit.jupiter.api.Assertions.*;

import com.musicmanager.model.Playlist;
import com.musicmanager.model.Track;
import com.musicmanager.repository.PlaylistRepository;
import com.musicmanager.repository.TrackRepository;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/** Test JUnit per la rimozione di un brano musicale dal catalogo generale. */

class RemoveTrackCommandTest {

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

    /** Implementazione in-memory di TrackRepository usata per simulare la persistenza delle tracce durante i test. */

    private static class InMemoryTrackRepository implements TrackRepository {

        private final List<Track> storage = new ArrayList<>();

        @Override
        public List<Track> findAll() {
            return new ArrayList<>(storage);
        }

        @Override
        public void save(Track track) {
            if (!storage.contains(track)) {
                storage.add(track);
            }
        }

        @Override
        public void update(Track track) {
            int index = storage.indexOf(track);
            if (index >= 0) {
                storage.set(index, track);
            }
        }

        @Override
        public void delete(int id) {
            storage.removeIf(track -> track.getId() == id);
        }

    }

    private InMemoryTrackRepository repository;
    private InMemoryPlaylistRepository playlistRepository;
    private ObservableList<Track> trackList;
    private Track track;
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

    /** Inizializza i repository e i dati necessari ai diversi scenari dei test. */

    @BeforeEach
    void setUp() {

        repository = new InMemoryTrackRepository();
        playlistRepository = new InMemoryPlaylistRepository();
        track = new Track(1, "Bad Guy", "Billie Eilish", 194, "Pop", 2019);
        playlist = new Playlist(1, "Preferiti");
        playlist.addTrack(track);

        repository.save(track);
        playlistRepository.save(playlist);
        trackList = FXCollections.observableArrayList(track);

    }

    /** Verifica che la rimozione di una traccia aggiorni il catalogo delle tracce, il repository e le playlist coinvolte. */

    @Test
    void executeRemovesTrackFromListAndDeletesFromRepository() {

        RemoveTrackCommand command = new RemoveTrackCommand(track, repository, playlistRepository, trackList);

        command.execute();

        assertFalse(trackList.contains(track));
        assertTrue(repository.findAll().isEmpty());
        assertFalse(playlist.getTracks().contains(track));

    }

    /** Verifica che l'annullamento della rimozione di una traccia ripristini la traccia nel catalogo, nel repository e nelle playlist coinvolte. */

    @Test
    void undoRestoresTrackInListRepositoryAndAffectedPlaylists() {

        RemoveTrackCommand command = new RemoveTrackCommand(track, repository, playlistRepository, trackList);

        command.execute();
        command.undo();

        assertTrue(trackList.contains(track));
        assertTrue(repository.findAll().contains(track));
        assertTrue(playlist.getTracks().contains(track));
        assertEquals(playlist, playlistRepository.lastUpdatedPlaylist);

    }

    /** Verifica che l'undo del comando ripristini la traccia alla posizione originale nel catalogo e nelle playlist. */

    @Test
    void undoRestoresTrackToItsExactOriginalIndexInCatalogAndPlaylists() {

        Track trackPrima = new Track(10, "Traccia Prima", "Autore A", 180, "Pop", 2020);
        Track trackDopo = new Track(12, "Traccia Dopo", "Autore B", 200, "Pop", 2021);

        trackList.add(0, trackPrima);
        trackList.add(trackDopo);

        playlist.getTracks().add(0, trackPrima);
        playlist.getTracks().add(trackDopo);

        RemoveTrackCommand command = new RemoveTrackCommand(track, repository, playlistRepository, trackList);
        command.execute();
        
        assertFalse(trackList.contains(track));
        assertFalse(playlist.getTracks().contains(track));

        command.undo();

        assertEquals(1, trackList.indexOf(track), "La traccia deve essere ripristinata al suo indice originario nel catalogo principale.");
        assertEquals(1, playlist.getTracks().indexOf(track), "La traccia deve essere ripristinata al suo indice originario all'interno della playlist.");

    }

}