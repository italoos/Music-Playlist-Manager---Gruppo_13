package com.musicmanager.model;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.Objects;

public class Track {

    private int id;
    private String title;
    private String author;
    private int length;
    private String genre;
    private int year;
    private Set<Tag> tags;

    public Track(int id, String title, String author, int length, String genre, int year) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.length = length;
        this.genre = genre;
        this.year = year;
        this.tags = new HashSet<>();
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public int getLength() {
        return length;
    }

    public String getGenre() {
        return genre;
    }

    public int getYear() {
        return year;
    }


    public void setId(int id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public void setTags(Set<Tag> tags) {
        this.tags = new HashSet<>(tags);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Track track = (Track) o;
        return id == track.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public void addTag(Tag tag) {
        tags.add(tag);
    }
    
    public void removeTag(Tag tag) {
        tags.remove(tag);
    }
    
    public boolean hasTag(Tag tag) {
        return tags.contains(tag);
    }
    
    public Set<Tag> getTags() {
        return tags;
    }

/**
 *Converte l'insieme dei tag in una stringa da salvare nel database.
 * Ad esempio:
 * FAVOURITE,EXPLICIT, NEW_RELEASE
*/
public String serializeTags() {
    return tags.stream()
               .map(Tag::name)
               .collect(Collectors.joining(","));
}

/**
 * Ricostruisce l'insieme dei tag a partire dalla stringa letta dal database.
 * Ad esempio:
 * FAVOURITE,EXPLICIT, NEW_RELEASE
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