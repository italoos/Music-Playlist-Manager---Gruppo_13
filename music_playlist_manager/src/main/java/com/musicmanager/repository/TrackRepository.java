package com.musicmanager.repository;

import com.musicmanager.model.Track;
import java.util.List;

/**
 * Interfaccia per la gestione e la persistenza dei dati relativi ai brani musicali.
 *
 * Scelta architetturale - Repository Pattern:
 * Separa la logica applicativa dalla gestione dei dati, consentendo di modificare
 * il meccanismo di persistenza senza modificare il resto dell'applicazione.
 */

public interface TrackRepository {

    /**
     * Recupera tutti i brani presenti nel sistema di persistenza.
     */

    List<Track> findAll();

    /**
     * Inserisce un nuovo brano nel sistema di persistenza.
     */

    void save(Track track);

    /**
     * Aggiorna i dati di un brano già presente nel sistema di persistenza.
     */

    void update(Track track);

    /**
     * Elimina un brano dal sistema di persistenza.
     */

    void delete(int id);

}
