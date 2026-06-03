package com.musicmanager.controller;

import static org.junit.jupiter.api.Assertions.*;

import com.musicmanager.model.Track;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

class TrackFileParserTest {

    private final TrackFileParser parser = new TrackFileParser();

    @TempDir
    Path tempDir;

    @Test
    void parse_validTrackFile_returnsTrackWithExpectedFields() throws IOException {
        Path tempFile = tempDir.resolve("valid-track.txt");
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
    }

    @Test
    void parse_invalidTrackFile_throwsIllegalArgumentException() throws IOException {
        Path tempFile = tempDir.resolve("blank-fields-track.txt");
        Files.writeString(tempFile, "Title: \n" +
                "Author: \n" +
                "Length: 180\n" +
                "Genre: \n" +
                "Year: 2020\n");

        assertThrows(IllegalArgumentException.class, () -> parser.parse(tempFile.toFile()));
    }

    @Test
    void parse_missingLength_throwsIllegalArgumentException() throws IOException {
        Path tempFile = tempDir.resolve("missing-length-track.txt");
        Files.writeString(tempFile, "Title: Yellow\n" +
                "Author: Coldplay\n" +
                "Genre: Rock\n" +
                "Year: 2000\n");

        assertThrows(IllegalArgumentException.class, () -> parser.parse(tempFile.toFile()));
    }

    @Test
    void parse_missingYear_throwsIllegalArgumentException() throws IOException {
        Path tempFile = tempDir.resolve("missing-year-track.txt");
        Files.writeString(tempFile, "Title: Yellow\n" +
                "Author: Coldplay\n" +
                "Length: 269\n" +
                "Genre: Rock\n");

        assertThrows(IllegalArgumentException.class, () -> parser.parse(tempFile.toFile()));
    }

    @Test
    void parse_validTrackFile_trimsValuesAndIgnoresUnknownLines() throws IOException {
        Path tempFile = tempDir.resolve("track-with-spaces.txt");
        Files.writeString(tempFile, "Ignored: value\n" +
                "Title:   Billie Jean   \n" +
                "Author:   Michael Jackson   \n" +
                "Length:   293   \n" +
                "Genre:   Pop   \n" +
                "Year:   1982   \n");

        Track track = parser.parse(tempFile.toFile());

        assertEquals("Billie Jean", track.getTitle());
        assertEquals("Michael Jackson", track.getAuthor());
        assertEquals(293, track.getLength());
        assertEquals("Pop", track.getGenre());
        assertEquals(1982, track.getYear());
    }
}
