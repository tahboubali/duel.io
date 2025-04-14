package org.example;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import javax.swing.*;
import java.awt.*;
import java.util.*;

import static org.example.ConnectionHandler.*;
import static org.example.ConnectionHandler.ConnectionStatus.*;

public class SidePanel extends JPanel implements MessageObserver {
    private final ConnectionHandler connectionHandler;
    private final static Gson GSON = new Gson();
    private ArrayList<LeaderboardPlayer> leaderboard = new ArrayList<>();

    private static class LeaderboardPlayer {
        @SuppressWarnings("unused")
        private String username;
        @SuppressWarnings("unused")
        private double rank;

        public LeaderboardPlayer() {
        }

        public String getUsername() {
            return username;
        }

        public double getRank() {
            return rank;
        }
    }

    public SidePanel(ConnectionHandler connectionHandler) {
        this.connectionHandler = connectionHandler;
        connectionHandler.addObserver(this);
        var duelButton = new JButton("Enter Duel");
        setLayout(null);
        duelButton.setBounds(20, 80, 120, 30);
        add(duelButton);
        duelButton.addActionListener(_ -> {
            onDuelButtonClick();
            requestFocusInWindow();
        });
        addKeyListener(KeyHandler.getInstance());
        addMouseListener(MouseHandler.getInstance());
        setFocusable(true);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(Color.WHITE);
        var defaultFont = g.getFont();
        g.setFont(new Font(defaultFont.getFontName(), defaultFont.getStyle(), 20));
        g.drawString("Side Panel", 20, 35);
        g.setFont(defaultFont);
        drawConnectionStatus(g);
        int y = 140;
        int maxPlayers = 20;
        int playerOffset = 20;
        g.setColor(Color.WHITE);
        for (int i = 0; i < Math.min(leaderboard.size(), maxPlayers + 1); ++i, y += playerOffset) {
            var player = leaderboard.get(i);
            if (player.getUsername().equals(Main.getUsername())) {
                g.setColor(new Color(54, 155, 255));
            } else {
                g.setColor(Color.WHITE);
            }
            g.drawString(i + 1 + ". " + player.getUsername() + " (" + player.getRank() + ")", 20, y);
        }
    }

    private void drawConnectionStatus(Graphics g) {
        var label = "Connection: ";
        var status = connectionHandler.getConnectionStatus();
        if (Main.getUsername() == null) status = CONNECTING;
        g.setColor(Color.WHITE);
        g.drawString(label, 20, 60);
        var fm = g.getFontMetrics();
        int labelWidth = fm.stringWidth(label);
        g.setColor(switch (status) {
            case SUCCESS -> Color.GREEN;
            case CONNECTING -> Color.ORANGE;
            case FAILED -> Color.RED;
        });
        g.drawString(status.toString(), 20 + labelWidth, 60);
    }

    public void onDuelButtonClick() {
        connectionHandler.sendMessage(Map.of("request_type", "enter-duel"));
    }

    @Override
    public void handleMessage(Map<String, Object> message) {
        if (message.get("request_type").equals("players-update")) {
            leaderboard = GSON.fromJson(
                    GSON.toJson(message.get("players")),
                    new TypeToken<ArrayList<LeaderboardPlayer>>() {}.getType()
            );
        }
    }
}
