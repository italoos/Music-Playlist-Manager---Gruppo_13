package com.musicmanager.controller;

import static org.junit.jupiter.api.Assertions.*;

import com.musicmanager.model.Track;
import com.musicmanager.repository.TrackRepository;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

class RemoveTrackCommandTest {

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

    @BeforeEach
    void setUp() {
        repository = new InMemoryTrackRepository();
        track = new Track(1, "Bad Guy", "Billie Eilish", 194, "Pop", 2019);
        repository.save(track);
        trackList = FXCollections.observableArrayList(track);
    }

    @Test
    void executeRemovesTrackFromListAndDeletesFromRepository() {
        RemoveTrackCommand command = new RemoveTrackCommand(track, repository, trackList);

        command.execute();

        assertFalse(trackList.contains(track));
        assertTrue(repository.findAll().isEmpty());
    }
}
