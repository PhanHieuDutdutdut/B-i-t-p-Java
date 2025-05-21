package model;

public class Song {
    public int id;
    public String title;
    public double duration; // in minutes
    public String artist;
    public String lyrics;
    public String genre;
    public String filePath;

    // Constructors, getters, and setters
    public Song(int id, String title, double duration, String artist, String lyrics, String genre, String filePath) {
        this.id = id;
        this.title = title;
        this.duration = duration;
        this.artist = artist;
        this.lyrics = lyrics;
        this.genre = genre;
        this.filePath = filePath;
    }

    public int getId() { return id; }
    public String getTitle() { return title; }
    public double getDuration() { return duration; }
    public String getArtist() { return artist; }
    public String getLyrics() { return lyrics; }
    public String getGenre() { return genre; }
    public String getFilePath() { return filePath; }
}
