package org.example;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.Map;

import static java.lang.Thread.sleep;
import static java.lang.Thread.startVirtualThread;
import static org.example.ConnectionHandler.MessageObserver;

public class GamePanel extends JPanel implements Runnable, MessageObserver {
    public static final Dimension SIZE = Toolkit.getDefaultToolkit().getScreenSize();
    private static final int TARGET_FPS = 350;
    private Player player;
    private final PhysicsHandler physicsHandler;
    private final ConnectionHandler connectionHandler;
    private final TitleScreenPanel titleScreen;
    private boolean running;
    private int currFPS;

    public GamePanel() {
        setBackground(Color.DARK_GRAY);
        setPreferredSize(SIZE);
        addKeyListener(KeyHandler.getInstance());
        addMouseListener(MouseHandler.getInstance());
        setFocusable(true);
        requestFocusInWindow();
        this.connectionHandler = new ConnectionHandler();
        connectionHandler.addObserver(this);
        startVirtualThread(connectionHandler);
        titleScreen = new TitleScreenPanel(connectionHandler);
        add(titleScreen, BorderLayout.CENTER);
        this.physicsHandler = new PhysicsHandler(this);
    }

    @Override
    public int getWidth() {
        return super.getWidth();
    }

    private void setPlayer() {
        var input = titleScreen.getInput();
        var username = input.get("username");
        var player = new Player(this, username);
        this.player = player;
        addPhysicsObject(player);
        Main.setUsername(username);
        remove(titleScreen);
    }

    public void run() {
        setPlayer();
        double drawInterval = 1_000_000_000. / (TARGET_FPS);
        double nextDrawTime = System.nanoTime() + drawInterval;
        double last = System.currentTimeMillis();
        if (running)
            throw new IllegalStateException("Game is already running.");

        running = true;
        long lastFPSCheckMillis = System.currentTimeMillis();
        int frames = 0;
        while (running) {
            frames++;
            double now = System.currentTimeMillis();
            update(now - last);
            repaint();
            if (now - lastFPSCheckMillis >= 500) {
                currFPS = (int) Math.round(1000d / ((now - lastFPSCheckMillis) / frames));
                lastFPSCheckMillis = Math.round(now);
                frames = 0;
            }
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
        super.paintComponent(g);
        var g2d = (Graphics2D) g;
        g2d.setColor(Color.WHITE);
        g2d.drawString("FPS: " + currFPS, 30, 50);
        if (player != null)
            player.draw(g2d);
        if (Arrays.stream(getComponents()).toList().contains(titleScreen))
            titleScreen.repaint();
    }

    public void addPhysicsObject(PhysicsObject object) {
        physicsHandler.trackObject(object);
    }

    public void handleMessage(Map<String, Object> message) {
    }

    public void createSidePanel() {
        var sidePanel = new SidePanel(connectionHandler);
        sidePanel.setSize(200, 700);
        sidePanel.setBackground(new Color(0, 0, 0, 150));
        sidePanel.setLocation(
                (int) Toolkit.getDefaultToolkit().getScreenSize().getWidth() - 10 - sidePanel.getWidth(),
                10
        );
        getParent().add(sidePanel, JLayeredPane.PALETTE_LAYER);
        addKeyListener(KeyHandler.getInstance());
        setFocusable(true);
    }
}
