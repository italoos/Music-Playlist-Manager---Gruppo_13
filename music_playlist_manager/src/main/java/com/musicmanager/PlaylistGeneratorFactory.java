package com.musicmanager;

public class PlaylistGeneratorFactory {

    public static PlaylistGenerator getGenerator(String type, String criteria) {
        if (type == null) {
            throw new IllegalArgumentException("Il tipo di generazione non può essere null");
        }

        switch (type.toUpperCase()) {
            case "GENRE":
                return new PlaylistGenreGenerator(criteria);
            case "YEAR":
                try {
                    int year = Integer.parseInt(criteria);
                    return new PlaylistYearGenerator(year);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("L'anno inserito non è valido.");
                }
            default:
                throw new IllegalArgumentException("Tipo di generazione sconosciuto: " + type);
        }
    }
}