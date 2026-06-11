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
     *
     * playlist è la playlist da salvare
     */
    void save(Playlist playlist);

    /**
     * Aggiorna una playlist esistente identificata dal suo ID.
     *
     * playlist è la playlist con i nuovi dati
     */
    void update(Playlist playlist);

    /**
     * Aggiorna il nome una playlist esistente identificata dal suo ID.
     *
     * playlist è la playlist con i nuovi dati
     */
    void updateName(Playlist playlist);

    /**
     * Elimina una playlist identificata dal suo ID.
     *
     * playlistId è l'identificativo della playlist da eliminare
     */
    void delete(int playlistId);

    /**
     * Cerca una playlist tramite ID.
     *
     * playlistId è l'identificativo della playlist da cercare
     * Restituisce la playlist trovata, oppure null se non esiste
     */
    Playlist findById(int playlistId);

    /**
     * Restituisce tutte le playlist presenti nel sistema.
     *
     * Restituisce lista di tutte le playlist
     */
    List<Playlist> findAll();
}