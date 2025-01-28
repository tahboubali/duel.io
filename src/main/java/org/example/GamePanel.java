package org.example;

import javax.swing.*;
import java.awt.*;
import java.util.Map;
import java.util.List;

import static java.lang.Thread.sleep;
import static java.lang.Thread.startVirtualThread;

public class GamePanel extends JPanel implements Runnable {
    public static final Dimension SIZE = Toolkit.getDefaultToolkit().getScreenSize();
    private static final int TARGET_FPS = 250;
    private Player player;
    private final PhysicsHandler physicsHandler;
    private final ConnectionHandler connectionHandler;
    private final TitleScreenPanel titleScreen;
    private final DuelManager duelManager;
    private boolean running;

    public GamePanel() {
        setBackground(Color.DARK_GRAY);
        setPreferredSize(SIZE);
        addKeyListener(KeyHandler.getInstance());
        addMouseListener(MouseHandler.getInstance());
        setFocusable(true);
        this.connectionHandler = new ConnectionHandler();
        startVirtualThread(connectionHandler);
        this.duelManager = new DuelManager(connectionHandler);
        titleScreen = new TitleScreenPanel();
        add(titleScreen, BorderLayout.CENTER);
        this.physicsHandler = new PhysicsHandler(this);
    }

    private void setPlayer() {
        var input = titleScreen.getInput();
        var username = input.get("username");
        var player = new Player(this, username);
        this.player = player;
        addPhysicsObject(player);
        connectionHandler.sendMessage(Map.of(
                "request_type", "new-player",
                "data", Map.of(
                        "username", username
                )
        ));
        remove(titleScreen);
    }

    public void run() {
        setPlayer();
        double drawInterval = 1_000_000_000d / TARGET_FPS;
        double nextDrawTime = System.nanoTime() + drawInterval;
        double last = System.currentTimeMillis();
        if (running)
            throw new IllegalStateException("Game is already running.");

        running = true;

        while (running) {
            double now = System.currentTimeMillis();
            update(now - last);
            repaint();
            last = now;
            try {
                double curr = System.nanoTime();
                double remainingTime = nextDrawTime - curr;
                remainingTime /= 1000000;

                if (remainingTime < 0)
                    remainingTime = 0;

                sleep((long) remainingTime);

                nextDrawTime += drawInterval;

            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void update(double dt) {
        player.update(dt);
        physicsHandler.update(dt);
    }

    protected void paintComponent(Graphics g) {
        var g2d = (Graphics2D) g;
        if (player != null)
            player.draw(g2d);
    }

    public void addPhysicsObject(PhysicsObject object) {
        physicsHandler.trackObject(object);
    }

    public List<PhysicsObject> getPhysicsObjects() {
        return physicsHandler.getObjects();
    }
}
