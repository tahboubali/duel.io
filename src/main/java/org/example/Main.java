package org.example;

import javax.swing.*;
import java.awt.*;

import static java.lang.Thread.startVirtualThread;

public class Main {
    private static String username;
    private static GamePanel gamePanel;
    private static JFrame window;

    public static void main(String[] args) {
        run();
    }

    private static void run() {
        var window = new JFrame();
        Main.window = window;
        window.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        window.setSize(Toolkit.getDefaultToolkit().getScreenSize());
        window.setBackground(new Color(19, 19, 19));
        var layeredPane = new JLayeredPane();
        layeredPane.setPreferredSize(window.getSize());
        var game = new GamePanel();
        var size = window.getSize();
        game.setSize(new Dimension(size.width - 70, size.height - 70));
        game.setLocation(size.width / 2 - game.getWidth() / 2, size.height / 2 - game.getHeight() / 2);
        layeredPane.add(game, JLayeredPane.DEFAULT_LAYER);
        game.createSidePanel();
        window.setContentPane(layeredPane);
        window.pack();
        window.setLocationRelativeTo(null);
        window.setVisible(true);
        setGamePanel(game);
        startVirtualThread(game);
    }


    public static String getUsername() {
        return username;
    }

    public static void setUsername(String username) {
        Main.username = username;
    }

    public static GamePanel getGamePanel() {
        return gamePanel;
    }

    public static void setGamePanel(GamePanel gamePanel) {
        Main.gamePanel = gamePanel;
    }

    public static JFrame getWindow() {
        return window;
    }
}