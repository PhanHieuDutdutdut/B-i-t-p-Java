package view;

import java.awt.*;
import java.util.List;
import javax.swing.*;
import model.Song;

public class MusicPlayerView extends JFrame {
    public JButton playButton;
    public JButton stopButton;
    public JTextArea lyricsArea;
    public JList<Song> songList;
    private DefaultListModel<Song> songListModel;

    public MusicPlayerView() {
        setTitle("Music Player");
        setSize(400, 300);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        songListModel = new DefaultListModel<>();
        songList = new JList<>(songListModel);
        playButton = new JButton("Play");
        stopButton = new JButton("Stop");
        lyricsArea = new JTextArea();
lyricsArea.setEditable(false);

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(playButton);
        buttonPanel.add(stopButton);

        add(new JScrollPane(songList), BorderLayout.NORTH);
        add(new JScrollPane(lyricsArea), BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    public void setSongs(List<Song> songs) {
        songListModel.clear();
        for (Song song : songs) {
            songListModel.addElement(song);
        }
    }
}