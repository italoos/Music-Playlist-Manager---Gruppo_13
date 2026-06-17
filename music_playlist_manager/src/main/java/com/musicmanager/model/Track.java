package com.musicmanager.model;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.Objects;

/**
 * Rappresenta un brano musicale gestito dall'applicazione.
 *
 * Contiene i dati principali del brano, i tag associati e il contatore
 * delle riproduzioni usato per ordinare le tracce piu ascoltate.
 */
public class Track {

    private int id;
    private String title;
    private String author;
    private int length;
    private String genre;
    private int year;
    private Set<Tag> tags;
    private int playCount;

    /**
     * Crea una traccia con tutti i campi necessari alla gestione e alla persistenza.
     * @param id Identificativo della traccia.
     * @param title Titolo del brano.
     * @param author Autore o artista del brano.
     * @param length Durata del brano in secondi.
     * @param genre Genere musicale.
     * @param year Anno di pubblicazione.
     * @param playCount Numero di riproduzioni registrate.
     */
    public Track(int id, String title, String author, int length, String genre, int year, int playCount) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.length = length;
        this.genre = genre;
        this.year = year;
        this.tags = new HashSet<>();
        this.playCount = playCount;
    }

    /**
     * Restituisce l'identificativo della traccia.
     * @return L'ID della traccia.
     */
    public int getId() {
        return id;
    }

    /**
     * Restituisce il titolo del brano.
     * @return Il titolo della traccia.
     */
    public String getTitle() {
        return title;
    }

    /**
     * Restituisce l'autore del brano.
     * @return L'autore o artista della traccia.
     */
    public String getAuthor() {
        return author;
    }

    /**
     * Restituisce la durata del brano.
     * @return La durata della traccia in secondi.
     */
    public int getLength() {
        return length;
    }

    /**
     * Restituisce il genere musicale del brano.
     * @return Il genere della traccia.
     */
    public String getGenre() {
        return genre;
    }

    /**
     * Restituisce l'anno di pubblicazione del brano.
     * @return L'anno della traccia.
     */
    public int getYear() {
        return year;
    }


    /**
     * Imposta l'identificativo della traccia.
     * @param id Il nuovo ID della traccia.
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Imposta il titolo del brano.
     * @param title Il nuovo titolo della traccia.
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Imposta l'autore del brano.
     * @param author Il nuovo autore della traccia.
     */
    public void setAuthor(String author) {
        this.author = author;
    }

    /**
     * Imposta la durata del brano.
     * @param length La nuova durata in secondi.
     */
    public void setLength(int length) {
        this.length = length;
    }

    /**
     * Imposta il genere musicale del brano.
     * @param genre Il nuovo genere della traccia.
     */
    public void setGenre(String genre) {
        this.genre = genre;
    }

    /**
     * Imposta l'anno di pubblicazione del brano.
     * @param year Il nuovo anno della traccia.
     */
    public void setYear(int year) {
        this.year = year;
    }

    /**
     * Sostituisce l'insieme dei tag associati alla traccia.
     * @param tags I tag da associare alla traccia.
     */
    public void setTags(Set<Tag> tags) {
        this.tags = new HashSet<>(tags);
    }

    /**
     * Restituisce il numero di riproduzioni della traccia.
     * @return Il contatore delle riproduzioni.
     */
    public int getPlayCount() {
        return playCount;
    }

    /**
     * Imposta il numero di riproduzioni della traccia.
     * @param playCount Il nuovo contatore delle riproduzioni.
     */
    public void setPlayCount(int playCount) {
        this.playCount = playCount;
    }

    /**
     * Incrementa di una unita il numero di riproduzioni della traccia.
     */
    public void incrementPlayCount(){
        this.playCount ++;
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Track track = (Track) o;
        return id == track.id;
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    /**
     * Aggiunge un tag alla traccia.
     * @param tag Il tag da aggiungere.
     */
    public void addTag(Tag tag) {
        tags.add(tag);
    }
    
    /**
     * Rimuove un tag dalla traccia.
     * @param tag Il tag da rimuovere.
     */
    public void removeTag(Tag tag) {
        tags.remove(tag);
    }
    
    /**
     * Verifica se la traccia possiede un tag specifico.
     * @param tag Il tag da cercare.
     * @return true se il tag e presente, false altrimenti.
     */
    public boolean hasTag(Tag tag) {
        return tags.contains(tag);
    }
    
    /**
     * Restituisce i tag associati alla traccia.
     * @return L'insieme dei tag della traccia.
     */
    public Set<Tag> getTags() {
        return tags;
    }

    /**
     * Converte l'insieme dei tag in una stringa da salvare nel database.
     * @return I tag serializzati separati da virgola.
     */
    public String serializeTags() {
        return tags.stream()
                .map(Tag::name)
                .collect(Collectors.joining(","));
    }

    /**
     * Ricostruisce l'insieme dei tag a partire dalla stringa letta dal database.
     * @param serializedTags Stringa contenente i tag separati da virgola.
     */
    public void deserializeTags(String serializedTags) {

        tags.clear();

        if (serializedTags == null || serializedTags.isBlank()) {
            return;
        }

        Arrays.stream(serializedTags.split(","))
            .map(String::trim)
            .map(Tag::valueOf)
            .forEach(tags::add);
    }
}

