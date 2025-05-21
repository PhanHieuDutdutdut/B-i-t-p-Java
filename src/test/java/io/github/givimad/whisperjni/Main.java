import controller.MusicPlayerController;
import javax.swing.*;
import model.DatabaseManager;
import view.MusicPlayerView;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                DatabaseManager db = new DatabaseManager();
                MusicPlayerView view = new MusicPlayerView();
                new MusicPlayerController(db, view);
                view.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}