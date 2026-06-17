package com.musicmanager.repository;

import java.util.List;

import com.musicmanager.model.Playlist;

/**
 * Repository che gestisce la persistenza e il recupero delle Playlist.
 * Fornisce operazioni CRUD e query di ricerca basate su ID.
 */
public interface PlaylistRepository {

    /**
     * Salva una nuova playlist nel sistema.
     * @param playlist La playlist da salvare.
     */
    void save(Playlist playlist);

    /**
     * Aggiorna una playlist esistente identificata dal suo ID.
     * @param playlist La playlist con i dati aggiornati.
     */
    void update(Playlist playlist);

    /**
     * Elimina una playlist identificata dal suo ID.
     * @param playlistId Identificativo della playlist da eliminare.
     */
    void delete(int playlistId);

    /**
     * Cerca una playlist tramite ID.
     * @param playlistId Identificativo della playlist da cercare.
     * @return La playlist trovata, oppure null se non esiste.
     */
    Playlist findById(int playlistId);

    /**
     * Restituisce tutte le playlist presenti nel sistema.
     * @return La lista di tutte le playlist.
     */
    List<Playlist> findAll();

    /**
     * Trova tutte le playlist in ordine del numero di riproduzione
     * @return la lista di playlist in ordine di numero di riproduzioni
     */
    List<Playlist> findAllByPlayCount();
}
