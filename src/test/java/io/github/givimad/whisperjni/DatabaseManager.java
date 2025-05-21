package model;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {
    private static final String PROJECT_DIR = System.getProperty("user.dir");
    private static final String DB_FILE_NAME = "music_db.db";
    private static final String DB_URL = "jdbc:sqlite:" + PROJECT_DIR + File.separator + DB_FILE_NAME + ";encoding=UTF-8";

    static {
        System.out.println("Using database file at: " + PROJECT_DIR + File.separator + DB_FILE_NAME);
        initializeDatabase();
    }

    public DatabaseManager() {
        // Constructor rỗng, kết nối đã được khởi tạo trong static block
    }

    public static void initializeDatabase() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            String createSongsTable = "CREATE TABLE IF NOT EXISTS songs ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "title TEXT NOT NULL,"
                    + "duration REAL,"
                    + "artist TEXT,"
                    + "lyrics TEXT,"
                    + "genre TEXT,"
                    + "filePath TEXT UNIQUE)";
            String createSegmentsTable = "CREATE TABLE IF NOT EXISTS segments ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "songId INTEGER NOT NULL,"
                    + "startTime TEXT NOT NULL,"
                    + "endTime TEXT NOT NULL,"
                    + "lyrics TEXT,"
                    + "FOREIGN KEY(songId) REFERENCES songs(id))";
            stmt.execute(createSongsTable);
            stmt.execute(createSegmentsTable);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    public static void insertSong(Song song) throws SQLException {
        String sql = "INSERT INTO songs (title, duration, artist, lyrics, genre, filePath) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, song.getTitle());
            pstmt.setDouble(2, song.getDuration());
            pstmt.setString(3, song.getArtist());
            pstmt.setString(4, song.getLyrics());
            pstmt.setString(5, song.getGenre());
            pstmt.setString(6, song.getFilePath());
            pstmt.executeUpdate();
        }
    }

    public static List<Song> getAllSongs() throws SQLException {
        List<Song> songs = new ArrayList<>();
        String sql = "SELECT * FROM songs";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                songs.add(new Song(
                    rs.getInt("id"),
                    rs.getString("title"),
                    rs.getDouble("duration"),
                    rs.getString("artist"),
                    rs.getString("lyrics"),
                    rs.getString("genre"),
                    rs.getString("filePath")
                ));
            }
        }
        return songs;
    }

    public static List<Segment> getSegmentsBySongId(int songId) throws SQLException {
        List<Segment> segments = new ArrayList<>();
        String sql = "SELECT * FROM segments WHERE songId = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, songId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                segments.add(new Segment(
                    rs.getInt("id"),
                    rs.getString("startTime"),
                    rs.getString("endTime"),
                    rs.getString("lyrics")
                ));
            }
        }
        return segments;
    }

    public static void insertSegment(int songId, Segment segment) throws SQLException {
        String sql = "INSERT INTO segments (songId, startTime, endTime, lyrics) VALUES (?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, songId);
            pstmt.setString(2, segment.start);
            pstmt.setString(3, segment.end);
            pstmt.setString(4, segment.lyrics);
            pstmt.executeUpdate();
        }
    }

    public static void updateSegmentLyrics(int segmentId, String newLyrics) throws SQLException {
        String sql = "UPDATE segments SET lyrics = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newLyrics);
            pstmt.setInt(2, segmentId);
            pstmt.executeUpdate();
        }
    }

    public static int getSongIdByFilePath(String filePath) throws SQLException {
        String sql = "SELECT id FROM songs WHERE filePath = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, filePath);
            ResultSet rs = pstmt.executeQuery();
            return rs.next() ? rs.getInt("id") : -1;
        }
    }

    public static List<String> getAllMp3FileNames() throws SQLException {
        List<String> fileNames = new ArrayList<>();
        String sql = "SELECT title FROM songs";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                fileNames.add(rs.getString("title"));
            }
        }
        return fileNames;
    }

    public static String getFilePathByFileName(String fileName) throws SQLException {
        String sql = "SELECT filePath FROM songs WHERE title = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, fileName);
            ResultSet rs = pstmt.executeQuery();
            return rs.next() ? rs.getString("filePath") : "";
        }
    }
}