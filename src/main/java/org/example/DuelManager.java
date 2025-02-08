package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;

import org.example.ConnectionHandler.MessageObserver;

public class DuelManager implements MessageObserver {
    private final static int WIDTH = 350, HEIGHT = 275;
    private ConnectionHandler connectionHandler;
    private final JPanel duelPanel;
    private boolean pressed;

    public DuelManager(ConnectionHandler connectionHandler, GamePanel gamePanel) {
        this.connectionHandler = connectionHandler;
        connectionHandler.addObserver(this);
        duelPanel = new JPanel();
        duelPanel.setPreferredSize(new Dimension(WIDTH, HEIGHT));
        var gamePanelSize = gamePanel.getPreferredSize();
        duelPanel.setLocation(gamePanelSize.width - WIDTH - 20, gamePanelSize.height - HEIGHT - 20);
        gamePanel.add(duelPanel);
        duelPanel.setFocusable(true);
        duelPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                pressed = true;
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                pressed = false;
            }
        });
        duelPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 5));
        duelPanel.setVisible(true);
    }

    public boolean isPressed() {
        return pressed;
    }

    public void update() {
        duelPanel.setVisible(!KeyHandler.isHide());
    }

    public void draw() {
        duelPanel.repaint();
    }

    public void setLocation(Point point) {
        if (point == null) return;
        this.duelPanel.setLocation(point.x - WIDTH / 2, point.y - HEIGHT / 2);
    }

    @Override
    public void handleMessage(Map<String, Object> message) {

    }
}
