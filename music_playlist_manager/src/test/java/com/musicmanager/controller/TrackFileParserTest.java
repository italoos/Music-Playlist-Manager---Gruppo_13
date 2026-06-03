package com.musicmanager.controller;

import static org.junit.jupiter.api.Assertions.*;

import com.musicmanager.model.Track;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

class TrackFileParserTest {

    private final TrackFileParser parser = new TrackFileParser();

    @Test
    void parse_validTrackFile_returnsTrackWithExpectedFields() throws IOException {
        Path tempFile = Files.createTempFile("track", ".txt");
        Files.writeString(tempFile, "Title: Shape of You\n" +
                "Author: Ed Sheeran\n" +
                "Length: 233\n" +
                "Genre: Pop\n" +
                "Year: 2017\n");

        Track track = parser.parse(tempFile.toFile());

        assertNotNull(track);
        assertEquals("Shape of You", track.getTitle());
        assertEquals("Ed Sheeran", track.getAuthor());
        assertEquals(233, track.getLength());
        assertEquals("Pop", track.getGenre());
        assertEquals(2017, track.getYear());

        Files.deleteIfExists(tempFile);
    }

    @Test
    void parse_invalidTrackFile_throwsIllegalArgumentException() throws IOException {
        Path tempFile = Files.createTempFile("track", ".txt");
        Files.writeString(tempFile, "Title: \n" +
                "Author: \n" +
                "Length: 180\n" +
                "Genre: \n" +
                "Year: 2020\n");

        assertThrows(IllegalArgumentException.class, () -> parser.parse(tempFile.toFile()));

        Files.deleteIfExists(tempFile);
    }
}
