package controller;

import model.DatabaseManager;
import model.Segment;
import model.Song;
import view.MusicPlayerView;
import ws.schild.jave.*;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

// Giả sử whisper-jni có gói org.whispercpp
import org.whispercpp.Whisper; // Thay bằng tên gói thực tế của whisper-jni

public class MusicPlayerController {
    private DatabaseManager db;
    private MusicPlayerView view;
    private Process playerProcess;
    private Thread progressThread;
    private Whisper whisper;

    public MusicPlayerController(DatabaseManager db, MusicPlayerView view) throws Exception {
        this.db = db;
        this.view = view;

        // Khởi tạo Whisper với mô hình
        initializeWhisper();

        // Load danh sách bài hát
        List<Song> songs = db.getAllSongs();
        view.setSongs(songs);

        // Sự kiện Play
        view.playButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Song selectedSong = view.songList.getSelectedValue();
                if (selectedSong != null) {
                    playSong(selectedSong);
                    loadAndSyncLyrics(selectedSong);
                } else {
                    JOptionPane.showMessageDialog(view, "Vui lòng chọn một bài hát!");
                }
            }
        });

        // Sự kiện Stop
        view.stopButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                stopSong();
            }
        });
    }

    private void initializeWhisper() throws Exception {
        // Khởi tạo Whisper với đường dẫn đến file mô hình
        String modelPath = "models/ggml-small-q8_0.bin"; // Đường dẫn đến file bạn đã tải
        whisper = new Whisper(modelPath); // Giả định API của whisper-jni
        whisper.init(); // Khởi tạo mô hình
    }

    private void playSong(Song song) {
        try {
            if (playerProcess != null && playerProcess.isAlive()) {
                playerProcess.destroy();
            }
            File source = new File(song.getFilePath());
            if (!source.exists()) {
                JOptionPane.showMessageDialog(view, "File không tồn tại: " + song.getFilePath());
                return;
            }

            // Phát nhạc bằng FFmpeg
            ProcessBuilder pb = new ProcessBuilder("ffmpeg", "-i", song.getFilePath(), "-f", "null", "-");
            playerProcess = pb.start();

            // Kiểm tra và trích xuất lời nhạc bằng Whisper nếu chưa có
            int songId = db.getSongIdByFilePath(song.getFilePath());
            List<Segment> existingSegments = db.getSegmentsBySongId(songId);
            if (existingSegments.isEmpty()) {
                List<Segment> transcribedSegments = transcribeAudio(song);
                for (Segment segment : transcribedSegments) {
                    db.insertSegment(songId, segment);
                }
                view.lyricsArea.setText("Đã trích xuất lời nhạc bằng Whisper và lưu vào cơ sở dữ liệu.");
            }
            view.lyricsArea.setText("Đang phát: " + song.getTitle() + " - " + song.getArtist());
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(view, "Lỗi phát nhạc: " + ex.getMessage());
        }
    }

    private List<Segment> transcribeAudio(Song song) throws Exception {
        List<Segment> segments = new ArrayList<>();
        // Sử dụng Whisper để trích xuất lời nhạc
        String transcript = whisper.transcribe(song.getFilePath()); // Giả định phương thức transcribe
        // Tạm thời lưu toàn bộ lời nhạc vào một đoạn (cần cải tiến để phân đoạn thời gian)
        segments.add(new Segment("00:00:00", "99:99:99", transcript));
        return segments;
    }

    private void stopSong() {
        if (playerProcess != null && playerProcess.isAlive()) {
            playerProcess.destroy();
            if (progressThread != null) {
                progressThread.interrupt();
            }
            view.lyricsArea.setText("Đã dừng phát nhạc.");
        }
    }

    private void loadAndSyncLyrics(Song song) {
        try {
            int songId = db.getSongIdByFilePath(song.getFilePath());
            List<Segment> segments = db.getSegmentsBySongId(songId);
            if (segments.isEmpty()) {
                view.lyricsArea.append("\nKhông có lời nhạc cho bài hát này.");
                return;
            }

            progressThread = new Thread(() -> {
                try {
                    ProcessBuilder pb = new ProcessBuilder("ffmpeg", "-i", song.getFilePath(), "-f", "null", "-");
                    playerProcess = pb.start();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(playerProcess.getErrorStream()));
                    String line;
                    double currentTime = 0.0;

                    while ((line = reader.readLine()) != null && !Thread.currentThread().isInterrupted()) {
                        if (line.contains("time=")) {
                            String timeStr = line.substring(line.indexOf("time=") + 5, line.indexOf("time=") + 13);
                            currentTime = parseTimeToSeconds(timeStr);
                            updateLyrics(segments, currentTime);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            progressThread.start();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(view, "Lỗi tải lyrics: " + e.getMessage());
        }
    }

    private double parseTimeToSeconds(String timeStr) {
        String[] parts = timeStr.split(":");
        double hours = Double.parseDouble(parts[0]);
        double minutes = Double.parseDouble(parts[1]);
        double seconds = Double.parseDouble(parts[2]);
        return hours * 3600 + minutes * 60 + seconds;
    }

    private void updateLyrics(List<Segment> segments, double currentTime) {
        SwingUtilities.invokeLater(() -> {
            for (Segment segment : segments) {
                double start = parseTimeToSeconds(segment.getStart());
                double end = parseTimeToSeconds(segment.getEnd());
                if (currentTime >= start && currentTime <= end) {
                    view.lyricsArea.setText(segment.getLyrics());
                    break;
                }
            }
        });
    }
}