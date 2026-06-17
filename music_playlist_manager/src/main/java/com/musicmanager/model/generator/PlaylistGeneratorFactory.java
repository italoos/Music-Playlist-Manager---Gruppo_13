package com.musicmanager.model.generator;

/**
 * Factory per creare il generatore di playlist automatiche in base al criterio scelto.
 */
public class PlaylistGeneratorFactory {

    /**
     * Restituisce il generatore adatto al tipo di criterio richiesto.
     * @param type Tipo di generazione, ad esempio "GENRE" o "YEAR".
     * @param criteria Valore del criterio usato dal generatore.
     * @return Il generatore configurato per il criterio indicato.
     * @throws IllegalArgumentException Se il tipo e nullo, sconosciuto o se il criterio non e valido.
     */
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
