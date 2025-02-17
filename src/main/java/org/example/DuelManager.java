package org.example;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Map;

import org.example.ConnectionHandler.MessageObserver;

public class DuelManager implements MessageObserver {
    private final static int WIDTH = 350, HEIGHT = 275;
    private final ConnectionHandler connectionHandler;
    private final Point position;
    private final GamePanel gamePanel;
    private Vec2 delta;
    private boolean pressed;
    private boolean visible;

    public DuelManager(ConnectionHandler connectionHandler, GamePanel gamePanel) {
        this.connectionHandler = connectionHandler;
        position = new Point(gamePanel.getWidth() / 2 - WIDTH / 2, gamePanel.getHeight() / 2 - HEIGHT / 2);
        this.gamePanel = gamePanel;
        gamePanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (new Rectangle(position.x, position.y, WIDTH, HEIGHT).contains(e.getPoint())) {
                    pressed = true;
                    delta = Vec2.delta(e.getPoint(), position);
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                pressed = false;
            }
        });
        connectionHandler.addObserver(this);
        this.visible = true;
    }

    public void update() {
        visible = !KeyHandler.isHide();
        if (visible) {
            var mousePos = gamePanel.getMousePosition();
            if (pressed && mousePos != null) {
                position.setLocation(Vec2.of(mousePos).add(delta).asPoint());
            }
        }
    }

    public void draw(Graphics2D g2d) {
        if (visible) {
            int arcWidth = 30, arcHeight = 30;
            g2d.setColor(Color.WHITE);
            g2d.fillRoundRect(position.x, position.y, WIDTH, HEIGHT, arcWidth, arcHeight);
            g2d.setColor(Color.BLACK);
            g2d.setStroke(new BasicStroke(4.5f));
            g2d.drawRoundRect(position.x, position.y, WIDTH, HEIGHT, arcWidth, arcHeight);
        }
    }

    @Override
    public void handleMessage(Map<String, Object> message) {

    }
}
