package org.example;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Map;

import static java.lang.Thread.sleep;
import static org.example.ConnectionHandler.GSON;
import static org.example.ConnectionHandler.MessageObserver;

public class GamePanel extends JPanel implements Runnable, MessageObserver {
    public static final Dimension SIZE = Toolkit.getDefaultToolkit().getScreenSize();
    private static final int TARGET_FPS = 350;
    private final PhysicsHandler physicsHandler;
    private final ConnectionHandler connectionHandler;
    private final TitleScreen titleScreen;
    private boolean running;
    private int currFPS;
    private boolean dueling;
    private boolean matchmaking;
    private Player player;
    private Opponent opponent;
    private String prevUpdateJson;
    private long lastSendUpdate;
    private boolean sentGameEnd;
    private boolean autoDuelQueued;

    public GamePanel() {
        setBackground(Color.DARK_GRAY);
        int prevWidth = SIZE.width;
        SIZE.width -= 100 * SIZE.width / SIZE.height;
        SIZE.height -= 100 * prevWidth / SIZE.height;
        setPreferredSize(SIZE);
        addKeyListener(KeyHandler.getInstance());
        addMouseListener(MouseHandler.getInstance());
        setFocusable(true);
        requestFocusInWindow();
        this.connectionHandler = new ConnectionHandler();
        connectionHandler.addObserver(this);
        Thread connectionThread = new Thread(connectionHandler, "connection-handler");
        connectionThread.start();
        titleScreen = new TitleScreen(connectionHandler);
        add(titleScreen, BorderLayout.CENTER);
        this.physicsHandler = new PhysicsHandler(this);
    }

    @Override
    public int getWidth() {
        return super.getWidth();
    }

    private void setPlayer() {
        Map<String, String> input = titleScreen.getInput();
        String username = input.get("username");
        Player player = new Player(this, username);
        this.player = player;
        addPhysicsObject(player);
        Main.setUsername(username);
        remove(titleScreen);
    }

    public void run() {
        setPlayer();
        double drawInterval = 1_000_000_000. / TARGET_FPS;
        double nextDrawTime = System.nanoTime() + drawInterval;
        double last = System.currentTimeMillis();
        if (running) throw new IllegalStateException("Game is already running.");

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

                if (remainingTime < 0) remainingTime = 0;

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
        if (opponent != null) {
            opponent.update(dt);
        }
        maybeAutoEnterDuel();
        if (dueling && System.currentTimeMillis() - lastSendUpdate >= 20) {
            sendPlayerUpdate();
        }
        if (player.getHealth() <= 0 && !sentGameEnd && opponent != null) {
            connectionHandler.sendMessage(Maps.of(
                    "request_type", "game-end",
                    "data", Maps.of(
                            "player_won", opponent.getName()
                    )
            ));
            sentGameEnd = true;
        }
    }

    private void maybeAutoEnterDuel() {
        if (!BrowserHarnessBridge.isEnabled("autoduel") || autoDuelQueued || player == null || dueling || matchmaking) {
            return;
        }
        if (Main.getUsername() == null || connectionHandler.getConnectionStatus() != ConnectionHandler.ConnectionStatus.SUCCESS) {
            return;
        }
        autoDuelQueued = true;
        BrowserHarnessBridge.reportStatus("auto enter duel");
        connectionHandler.sendMessage(Maps.of("request_type", "enter-duel"));
    }

    private void sendPlayerUpdate() {
        String updateJson = GSON.toJson(player.getUpdateInfo());
        if (prevUpdateJson != null && prevUpdateJson.equals(updateJson)) {
            return;
        }
        connectionHandler.sendRawMessage("{\"request_type\":\"game-state\",\"data\":" + updateJson + "}");
        prevUpdateJson = updateJson;
        lastSendUpdate = System.currentTimeMillis();
    }

    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        Font previousFont = g2d.getFont();
        g2d.setFont(new Font(previousFont.getFontName(), previousFont.getStyle(), 40));
        FontMetrics metrics = g2d.getFontMetrics();
        String headerMessage;
        Color headerColor;
        if (matchmaking) {
            headerColor = new Color(6, 122, 30);
            headerMessage = "Matchmaking...";
        } else if (dueling && opponent != null) {
            headerColor = Color.RED;
            headerMessage = "Dueling \"" + opponent.getName() + "\"!";
        } else {
            headerMessage = "Enter a duel!";
            headerColor = Color.BLACK;
        }
        int width = metrics.stringWidth(headerMessage);
        g2d.setColor(headerColor);
        g2d.drawString(headerMessage, getWidth() / 2 - width / 2, 100);
        g2d.setColor(Color.WHITE);
        g2d.setFont(previousFont);
        g2d.drawString("FPS: " + currFPS, 30, 50);
        if (player != null) {
            player.draw(g2d);
        }
        if (opponent != null) {
            opponent.draw(g2d);
        }
    }

    public void addPhysicsObject(PhysicsObject object) {
        physicsHandler.trackObject(object);
    }

    public void handleMessage(Map<String, Object> message) {
        String requestType = (String) message.get("request_type");
        if ("enter-duel".equals(requestType)) {
            int status = ((Number) message.get("status")).intValue();
            if (status == 0) {
                matchmaking = true;
                dueling = false;
            } else {
                sentGameEnd = false;
                dueling = true;
                matchmaking = false;
                enterDuel(message);
            }
        } else if ("game-end".equals(requestType)) {
            matchmaking = false;
            dueling = false;
            prevUpdateJson = null;
            opponent.destroy();
            opponent = null;
            player.resetHealth();
            player.setProjectiles(new ArrayList<Projectile>());
            player.setBlocks(new ArrayList<Block>());
            physicsHandler.reset();
            addPhysicsObject(player);
        }
    }

    private void enterDuel(Map<String, Object> message) {
        String position = (String) message.get("position");
        prevUpdateJson = null;
        player.startDuel(position);
        opponent = new Opponent(this, (String) ((Map<?, ?>) message.get("match")).get("username"));
        connectionHandler.addObserver(opponent);
        opponent.startDuel(position.equals("left") ? "right" : "left");
    }

    public void createSidePanel() {
        SidePanel sidePanel = new SidePanel(connectionHandler);
        sidePanel.setSize(200, 700);
        sidePanel.setBackground(new Color(0, 0, 0, 150));
        sidePanel.setLocation((int) Toolkit.getDefaultToolkit().getScreenSize().getWidth() - 10 - sidePanel.getWidth(), 10);
        getParent().add(sidePanel, JLayeredPane.PALETTE_LAYER);
        addKeyListener(KeyHandler.getInstance());
        setFocusable(true);
    }

    public void changePlayerHealth(double delta) {
        player.setHealth(player.getHealth() + delta);
    }

    public Opponent getOpponent() {
        return opponent;
    }

    public void sendHealthDelta(double delta) {
        connectionHandler.sendMessage(Maps.of(
                "request_type", "health-update",
                "data", Maps.of(
                        "delta", delta
                )
        ));
    }
}
