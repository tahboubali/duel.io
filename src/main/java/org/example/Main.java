package org.example;

import javax.swing.*;

import java.awt.*;

import static java.lang.Thread.startVirtualThread;
import static org.example.PolyUtils.from;
import static org.example.PolyUtils.getCorners;

public class Main {
    public static void main(String[] args) {
        run();
    }

    private static void run() {
        var window = new JFrame();
        window.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
//        window.setUndecorated(true);
        var game = new GamePanel();
        window.add(game);
        window.pack();
        window.setLocationRelativeTo(null);
        window.setVisible(true);
        startVirtualThread(game);
    }
}