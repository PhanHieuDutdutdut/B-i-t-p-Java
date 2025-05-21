package view;

import java.awt.*;
import java.awt.event.*;
import java.sql.SQLException;
import java.util.List;
import javax.swing.*;
import model.DatabaseManager;
import model.Segment;
import model.Song;

public class LyricsEditorUI extends JFrame {
    public DefaultListModel<String> mp3ListModel = new DefaultListModel<>();
    public JList<String> mp3List = new JList<>(mp3ListModel);
    public JTextArea lyricsArea = new JTextArea(10, 40);
    public JTextField startTimeField = new JTextField(5);
    public JTextField endTimeField = new JTextField(5);
    public JButton btnAddSegment = new JButton("Thêm đoạn");
    public JButton btnUpdateLyrics = new JButton("Cập nhật lyrics");
    public JButton btnLoadFiles = new JButton("Tải lại danh sách");
    public JButton btnAddFile = new JButton("Thêm file");

    public LyricsEditorUI() {
        setTitle("Lyrics Editor");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.add(new JLabel("Danh sách MP3:"), BorderLayout.NORTH);
        leftPanel.add(new JScrollPane(mp3List), BorderLayout.CENTER);
        leftPanel.add(btnLoadFiles, BorderLayout.SOUTH);

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(new JLabel("Lyrics phân đoạn:"), BorderLayout.NORTH);
        centerPanel.add(new JScrollPane(lyricsArea), BorderLayout.CENTER);

        JPanel timePanel = new JPanel();
        timePanel.add(new JLabel("Bắt đầu:"));
        timePanel.add(startTimeField);
        timePanel.add(new JLabel("Kết thúc:"));
        timePanel.add(endTimeField);
        timePanel.add(btnAddSegment);
        centerPanel.add(timePanel, BorderLayout.SOUTH);

        JPanel bottomPanel = new JPanel();
        bottomPanel.add(btnUpdateLyrics);

        add(leftPanel, BorderLayout.WEST);
        add(centerPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        btnLoadFiles.addActionListener(e -> loadMp3List());
        btnAddFile.addActionListener(e -> addNewMp3());
        btnAddSegment.addActionListener(e -> insertSegment());
        btnUpdateLyrics.addActionListener(e -> updateSegmentLyrics());

        mp3List.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    loadSegmentsForSelectedFile();
                }
            }
        });

        leftPanel.add(btnAddFile, BorderLayout.NORTH);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    public void loadMp3List() {
        mp3ListModel.clear();
        try {
            List<String> fileNames = DatabaseManager.getAllMp3FileNames();
            for (String name : fileNames) {
                mp3ListModel.addElement(name);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Lỗi khi tải danh sách: " + e.getMessage());
        }
    }

    public void addNewMp3() {
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showOpenDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) return;

        String filePath = fileChooser.getSelectedFile().getAbsolutePath();
        String fileName = fileChooser.getSelectedFile().getName();

        // Nhập thông tin bài hát
        JTextField titleField = new JTextField(fileName, 20);
        JTextField artistField = new JTextField(20);
        JTextField genreField = new JTextField(20);
        JTextField durationField = new JTextField(5);

        JPanel inputPanel = new JPanel(new GridLayout(4, 2));
        inputPanel.add(new JLabel("Tên bài hát:"));
        inputPanel.add(titleField);
        inputPanel.add(new JLabel("Ca sĩ:"));
        inputPanel.add(artistField);
        inputPanel.add(new JLabel("Thể loại:"));
        inputPanel.add(genreField);
        inputPanel.add(new JLabel("Thời lượng (phút):"));
        inputPanel.add(durationField);

        int dialogResult = JOptionPane.showConfirmDialog(this, inputPanel, "Nhập thông tin bài hát", JOptionPane.OK_CANCEL_OPTION);
        if (dialogResult != JOptionPane.OK_OPTION) return;

        try {
            String title = titleField.getText();
            String artist = artistField.getText();
            String genre = genreField.getText();
            double duration = Double.parseDouble(durationField.getText());

            Song song = new Song(0, title, duration, artist, "", genre, filePath);
            DatabaseManager.insertSong(song);
            loadMp3List();
            JOptionPane.showMessageDialog(this, "Thêm bài hát thành công!");
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Thời lượng không hợp lệ!");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Lỗi khi thêm bài hát: " + e.getMessage());
        }
    }

    public void loadSegmentsForSelectedFile() {
        String fileName = mp3List.getSelectedValue();
        if (fileName == null) return;

        try {
            String filePath = DatabaseManager.getFilePathByFileName(fileName);
            int songId = DatabaseManager.getSongIdByFilePath(filePath);
            List<Segment> segments = DatabaseManager.getSegmentsBySongId(songId);

            StringBuilder sb = new StringBuilder();
            for (Segment s : segments) {
                sb.append(s.start).append(" - ").append(s.end).append(":\n").append(s.lyrics).append("\n\n");
            }
            lyricsArea.setText(sb.toString());
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Lỗi khi tải phân đoạn: " + e.getMessage());
        }
    }

    public void insertSegment() {
        String fileName = mp3List.getSelectedValue();
        if (fileName == null) return;

        try {
            String filePath = DatabaseManager.getFilePathByFileName(fileName);
            int songId = DatabaseManager.getSongIdByFilePath(filePath);

            Segment s = new Segment(startTimeField.getText(), endTimeField.getText(), lyricsArea.getText());
            DatabaseManager.insertSegment(songId, s);
            loadSegmentsForSelectedFile();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Lỗi khi thêm phân đoạn: " + e.getMessage());
        }
    }

    public void updateSegmentLyrics() {
        String input = JOptionPane.showInputDialog("Nhập ID đoạn cần sửa:");
        if (input == null || input.isEmpty()) return;

        try {
            int segmentId = Integer.parseInt(input);
            String newLyrics = lyricsArea.getText();
            DatabaseManager.updateSegmentLyrics(segmentId, newLyrics);
            JOptionPane.showMessageDialog(this, "Cập nhật thành công!");
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "ID không hợp lệ!");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Lỗi khi cập nhật: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LyricsEditorUI());
    }
}