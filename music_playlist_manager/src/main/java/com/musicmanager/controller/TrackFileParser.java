package com.musicmanager.controller;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;

import com.musicmanager.model.Track;

/**
 * Questa classe serve per leggere il file selezionato dall'utente,
 * estrarne titolo, autore, durata, genere e anno,
 * e costruire un oggetto Track.
 */

public class TrackFileParser {

    public Track parse(File file) throws IOException{
        String title = "";
        String author = "";
        int length = 0;
        String genre = "";
        int year = 0;

         try (BufferedReader reader = new BufferedReader(new FileReader(file))) {

            String line;

            while ((line = reader.readLine()) != null) {

                if (line.startsWith("Title:")) {
                    title = line.substring("Title:".length()).trim();
                }

                else if (line.startsWith("Author:")) {
                    author = line.substring("Author:".length()).trim();
                }

                else if (line.startsWith("Length:")) {
                    length = Integer.parseInt(
                            line.substring("Length:".length()).trim());
                }

                else if (line.startsWith("Genre:")) {
                    genre = line.substring("Genre:".length()).trim();
                }

                else if (line.startsWith("Year:")) {
                    year = Integer.parseInt(
                            line.substring("Year:".length()).trim());
                }
            }
         }


         // verifica per evitare di creare tracce con dati incompleti
         if(title.isBlank() || author.isBlank() || genre.isBlank()) {

            throw new IllegalArgumentException("File traccia non valido");
}

         return new Track(
            0,                  // id assegnato dal DB
                title,
                author,
                length,
                genre,
                year
         );

    }
    
}
