package com.musicmanager;

import com.musicmanager.model.Track;
import com.musicmanager.model.Tag;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class TrackTagTest {

    /**
     * Verifica che sia possibile aggiungere il tag FAVOURITE ad una traccia.
     */
    @Test
    void shouldAddFavouriteTag() {

        Track track = new Track(1, "Song", "Artist", 180, "Pop", 2024);

        track.addTag(Tag.FAVOURITE);

        assertTrue(track.hasTag(Tag.FAVOURITE));
    }

    /**
     * Verifica che sia possibile aggiungere il tag EXPLICIT ad una traccia.
     */
    @Test
    void shouldAddExplicitTag() {

        Track track = new Track(1, "Song", "Artist", 180, "Pop", 2024);

        track.addTag(Tag.EXPLICIT);

        assertTrue(track.hasTag(Tag.EXPLICIT));
    }

    /**
     * Verifica che sia possibile aggiungere il tag NEW_RELEASE ad una traccia.
     */
    @Test
    void shouldAddNewReleaseTag() {

        Track track = new Track(1, "Song", "Artist", 180, "Pop", 2024);

        track.addTag(Tag.NEW_RELEASE);

        assertTrue(track.hasTag(Tag.NEW_RELEASE));
    }

    /**
     * Verifica che una traccia possa avere contemporaneamente più tag.
     */
    @Test
    void shouldContainMultipleTags() {

        Track track = new Track(1, "Song", "Artist", 180, "Pop", 2024);

        track.addTag(Tag.FAVOURITE);
        track.addTag(Tag.EXPLICIT);
        track.addTag(Tag.NEW_RELEASE);

        assertEquals(3, track.getTags().size());

        assertTrue(track.hasTag(Tag.FAVOURITE));
        assertTrue(track.hasTag(Tag.EXPLICIT));
        assertTrue(track.hasTag(Tag.NEW_RELEASE));
    }

}
