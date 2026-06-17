package com.musicmanager.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Rappresenta una playlist composta da una lista ordinata di tracce.
 *
 * Mantiene anche l'identificativo usato dal database e il numero di
 * riproduzioni usato nelle classifiche delle playlist piu ascoltate.
 */
public class Playlist {

    private String name;
    private int playCount;
    private List<Track> tracks;
    private int id;

    /**
     * Crea una nuova playlist non ancora persistita.
     * @param name Nome della playlist.
     */
    public Playlist(String name) {
        this.name = name;
        this.playCount = 0;
        this.tracks = new ArrayList<>();
    }

    /**
     * Crea una playlist con dati gia presenti nel sistema di persistenza.
     * @param id Identificativo della playlist.
     * @param name Nome della playlist.
     * @param playCount Numero di riproduzioni registrate.
     */
    public Playlist(int id, String name, int playCount) {
        this.id = id;
        this.name = name;
        this.playCount = playCount;
        this.tracks = new ArrayList<>();
    }

    /**
     * Restituisce l'identificativo della playlist.
     * @return L'ID della playlist.
     */
    public int getId() {
        return id;
    }

    /**
     * Imposta l'identificativo della playlist.
     * @param id Il nuovo ID della playlist.
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Restituisce il nome della playlist.
     * @return Il nome della playlist.
     */
    public String getName() {
        return name;
    }

    /**
     * Aggiunge una traccia alla playlist se non e gia presente.
     * @param track La traccia da aggiungere.
     */
    public void addTrack(Track track) {
        if (!tracks.contains(track)) {
            tracks.add(track);
        }
    }

    /**
     * Rimuove una traccia dalla playlist.
     * @param track La traccia da rimuovere.
     */
    public void removeTrack(Track track) {
        tracks.remove(track);
    }

    /**
     * Restituisce le tracce della playlist.
     * @return La lista ordinata delle tracce.
     */
    public List<Track> getTracks() {
        return tracks;
    }

    /**
     * Restituisce la posizione di una traccia nella playlist.
     * @param track La traccia da cercare.
     * @return L'indice della traccia, oppure -1 se non e presente.
     */
    public int indexOf(Track track) {
        return tracks.indexOf(track);
    }

    /**
     * Imposta il nome della playlist.
     * @param name Il nuovo nome della playlist.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Restituisce il numero di riproduzioni della playlist.
     * @return Il contatore delle riproduzioni.
     */
    public int getPlayCount() {
        return this.playCount;
    }

    /**
     * Imposta il numero di riproduzioni della playlist.
     * @param playCount Il nuovo contatore delle riproduzioni.
     */
    public void setPlayCount(int playCount) {
        this.playCount = playCount;
    }

    /**
     * Incrementa di una unita il numero di riproduzioni della playlist.
     */
    public void incrementPlayCount() {
        this.playCount++;
    }
}
