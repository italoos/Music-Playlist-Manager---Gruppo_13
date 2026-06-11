package com.musicmanager.controller;

import static org.junit.jupiter.api.Assertions.*;

import com.musicmanager.model.Track;
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

/** Test JUnit per l'aggiunta di un brano musicale nel catalogo generale. */

class AddTrackCommandTest {

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
    private ObservableList<Track> trackList;
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
        repository = new InMemoryTrackRepository();
        trackList = FXCollections.observableArrayList();
        track = new Track(1, "Bad Guy", "Billie Eilish", 194, "Pop", 2019);
    }

    /** Verifica che l'aggiunta di una traccia aggiorni il catalogo delle tracce e il repository. */

    @Test
    void executeAddsTrackToListAndSavesToRepository() {

        AddTrackCommand command = new AddTrackCommand(track, repository, trackList);

        command.execute();

        assertTrue(trackList.contains(track));
        assertTrue(repository.findAll().contains(track));
        assertEquals(1, trackList.size());
        assertEquals(1, repository.findAll().size());

    }

    /** Verifica che l'annullamento dell'aggiunta di una traccia rimuova la traccia dal catalogo e dal repository. */

    @Test
    void undoRemovesTrackFromListAndDeletesFromRepository() {

        AddTrackCommand command = new AddTrackCommand(track, repository, trackList);

        command.execute();
        command.undo();

        assertFalse(trackList.contains(track));
        assertTrue(repository.findAll().isEmpty());
        assertTrue(trackList.isEmpty());

    }

}