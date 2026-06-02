package com.musicmanager.model;

import java.util.Set;

public class Track {/* */

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

    public int getPlayCount() {
        return playCount;
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

    public void incrementPlayCount() {
        playCount++;
    }

    public void addTag(Tag tag) {
        if (tags != null) {
            tags.add(tag);
        }
    }

    public Set<Tag> getTags() {
        return tags;
    }
}
