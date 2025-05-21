package model;
// Mp3File.java
public class Mp3File {
    private final int id;
    private final String name;
    private final String path;

    public Mp3File(int id, String name, String path) {
        this.id = id;
        this.name = name;
        this.path = path;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getPath() { return path; }

    @Override
    public String toString() { return name; }

}

