package org.example;

import javax.swing.*;
import java.awt.*;

import static java.lang.Thread.startVirtualThread;

public class Main {
    private static String username;
    private static GamePanel gamePanel;

    public static void main(String[] args) {
        run();
    }

    private static void run() {
        var window = new JFrame();
        window.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        window.setSize(Toolkit.getDefaultToolkit().getScreenSize());
        window.setUndecorated(true);
        var layeredPane = new JLayeredPane();
        layeredPane.setPreferredSize(window.getSize());
        var game = new GamePanel();
        game.setSize(window.getSize());
        game.setLocation(0, 0);
        layeredPane.add(game, JLayeredPane.DEFAULT_LAYER);
        game.createSidePanel();
        window.setContentPane(layeredPane);
        window.pack();
        window.setLocationRelativeTo(null);
        window.setExtendedState(JFrame.MAXIMIZED_BOTH);
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
}