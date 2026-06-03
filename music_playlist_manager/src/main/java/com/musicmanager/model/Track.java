package com.musicmanager.model;

public class Track {

    private int id;
    private String title;
    private String author;
    private int length;
    private String genre;
    private int year;

    public Track(int id, String title, String author, int length, String genre, int year) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.length = length;
        this.genre = genre;
        this.year = year;
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
}
