package org.example;

import javax.swing.*;
import java.awt.*;
import java.time.Duration;
import java.util.Map;

public class TitleScreenPanel extends JPanel {
    private final int WIDTH = 500, HEIGHT = 500;

    public TitleScreenPanel() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setLayout(null);
        var l = new JLabel("Enter a username");
        l.setBounds(50, 100, 120, 30);
        add(l);
    }

    @Override
    protected void paintComponent(Graphics g) {
        g.setFont(new Font(getFont().getFontName(), getFont().getStyle(), 50));
        var titleString = "DUEL GAME";
        var bounds = g.getFontMetrics().getStringBounds(titleString, g);
        var width = (int) bounds.getWidth();
        var height = (int) bounds.getHeight();
        int heightOffset = 100;
        g.drawString(titleString, WIDTH / 2 - width / 2, HEIGHT / 2 - height / 2 - heightOffset);
    }


    public Map<String, String> getInput() {
        try {
            Thread.sleep(Duration.ofSeconds(0));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return Map.of("username", "tahboubali");
    }
}
