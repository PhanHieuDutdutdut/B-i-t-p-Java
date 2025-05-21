package model;

public class Segment {
    public int id;
    public String start;
    public String end;
    public String lyrics;

    public Segment(int id, String start, String end, String lyrics) {
        this.id = id;
        this.start = start;
        this.end = end;
        this.lyrics = lyrics;
    }

    public Segment(String start, String end, String lyrics) {
        this(-1, start, end, lyrics);
    }

    public String getStart() { return start; }
    public String getEnd() { return end; }
    public String getLyrics() { return lyrics; }
}