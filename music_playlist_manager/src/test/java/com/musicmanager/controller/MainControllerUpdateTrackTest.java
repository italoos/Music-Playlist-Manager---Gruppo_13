package com.musicmanager.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.musicmanager.model.Track;
import com.musicmanager.repository.TrackRepository;
import java.util.ArrayList;
import java.util.List;
import javafx.collections.ObservableList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MainControllerUpdateTrackTest {

    private static class InMemoryTrackRepository implements TrackRepository {

        private final List<Track> storage = new ArrayList<>();
        private Track lastUpdatedTrack;

        @Override
        public List<Track> findAll() {
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

    @BeforeEach
    void setUp() {
        repository = new InMemoryTrackRepository();
        controller = new MainController(repository, null);

        originalTrack = new Track(1, "Bad Guy", "Billie Eilish", 194, "Pop", 2019);
        repository.save(originalTrack);
        controller.getTracks().add(originalTrack);
    }

    @Test
    void handleUpdateTrackUpdatesTrackFieldsListAndRepository() {
        Track updatedTrack = new Track(1, "Ocean Eyes", "Billie Eilish", 180, "Alternative", 2016);

        controller.handleUpdateTrack(originalTrack, updatedTrack);

        assertEquals("Ocean Eyes", originalTrack.getTitle());
        assertEquals("Billie Eilish", originalTrack.getAuthor());
        assertEquals(180, originalTrack.getLength());
        assertEquals("Alternative", originalTrack.getGenre());
        assertEquals(2016, originalTrack.getYear());

        ObservableList<Track> tracks = controller.getTracks();
        assertEquals(1, tracks.size());
        assertEquals(originalTrack, tracks.get(0));
        assertEquals(originalTrack, repository.findAll().get(0));
        assertEquals(originalTrack, repository.lastUpdatedTrack);
    }

    @Test
    void handleUpdateTrackWithInvalidInputDoesNotChangeTrackOrRepository() {
        controller.handleUpdateTrack(originalTrack, null);
        controller.handleUpdateTrack(null, new Track(1, "Ocean Eyes", "Billie Eilish", 180, "Alternative", 2016));

        assertEquals("Bad Guy", originalTrack.getTitle());
        assertEquals("Billie Eilish", originalTrack.getAuthor());
        assertEquals(194, originalTrack.getLength());
        assertEquals("Pop", originalTrack.getGenre());
        assertEquals(2019, originalTrack.getYear());
        assertNull(repository.lastUpdatedTrack);
    }
}
