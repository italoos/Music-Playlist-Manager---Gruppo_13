package com.musicmanager;
public class Track {

    private String title;
    private String author;
    private int length;
    private String genre;
    private int year;

    public Track(String title, String author,
                 int length, String genre, int year) {

        this.title = title;
        this.author = author;
        this.length = length;
        this.genre = genre;
        this.year = year;
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
}