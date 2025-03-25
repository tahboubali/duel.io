package org.example;

import javax.swing.*;
import java.awt.*;

import static java.lang.Thread.startVirtualThread;

public class Main {
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

        var sidePanel = new SidePanel();
        sidePanel.setSize(200, 700);
        sidePanel.setBackground(new Color(0, 0, 0, 150));
        sidePanel.setLocation(10, 10);

        layeredPane.add(game, JLayeredPane.DEFAULT_LAYER);
        layeredPane.add(sidePanel, JLayeredPane.PALETTE_LAYER);

        window.setContentPane(layeredPane);
        window.pack();
        window.setLocationRelativeTo(null);
        window.setVisible(true);

        startVirtualThread(game);
    }

}
