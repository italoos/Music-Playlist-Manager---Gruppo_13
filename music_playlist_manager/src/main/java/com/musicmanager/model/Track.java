package com.musicmanager.model;

import java.util.Set;

public class Track {

    @SuppressWarnings("unused")
    private int id;
    @SuppressWarnings("unused")
    private String title;
    @SuppressWarnings("unused")
    private String author;
    @SuppressWarnings("unused")
    private int length;
    @SuppressWarnings("unused")
    private String genre;
    @SuppressWarnings("unused")
    private int year;
    @SuppressWarnings("unused")
    private int playCount;
    @SuppressWarnings("unused")
    private Set<Tag> tags;

    public Track(int id, String title, String author, int length, String genre, int year, int playCount, Set<Tag> tags) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.length = length;
        this.genre = genre;
        this.year = year;
        this.playCount = playCount;
        this.tags = tags;
    }

    public int getId() {
        // TODO getId
        return 0;
    }

    public String getTitle() {
        // TODO getTitle
        return null;
    }

    public String getAuthor() {
        // TODO getAuthor
        return null;
    }

    public int getLength() {
        // TODO getLength
        return 0;
    }

    public String getGenre() {
        // TODO getGenre
        return null;
    }

    public int getYear() {
        // TODO getYear
        return 0;
    }

    public int getPlayCount() {
        // TODO getPlayCount
        return 0;
    }

    public void setId(int id) {
        // TODO setId
    }

    public void setTitle(String title) {
        // TODO setTitle
    }

    public void setAuthor(String author) {
        // TODO setAuthor
    }

    public void setLength(int length) {
        // TODO setLength
    }

    public void setGenre(String genre) {
        // TODO setGenre
    }

    public void setYear(int year) {
        // TODO setYear
    }

    public void incrementPlayCount() {
        // TODO incrementPlayCount
    }

    public void addTag(Tag tag) {
        // TODO addTag
    }

    public Set<Tag> getTags() {
        // TODO getTags
        return null;
    }
}
