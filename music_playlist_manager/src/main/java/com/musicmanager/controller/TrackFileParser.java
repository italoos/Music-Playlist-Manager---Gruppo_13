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

    /**
     * Parsa il file di testo specificato e crea un oggetto Track con i dati estratti.
     * @param file Il file di testo da parsare, contenente le informazioni della traccia.
     * @return Un oggetto Track costruito con i dati estratti dal file.
     * @throws IOException Se si verifica un errore durante la lettura del file.
     */
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
        if(title.isBlank() || author.isBlank() || genre.isBlank() || length <= 0 || year <= 0) {

            throw new IllegalArgumentException("File traccia non valido");
        }


        return new Track(0, title, author, length, genre, year);

    }
    
}
