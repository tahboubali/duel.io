package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.time.Duration;
import java.util.Map;

import static javax.swing.JOptionPane.showMessageDialog;

public class TitleScreenPanel extends JPanel implements ConnectionHandler.MessageObserver {
    private final int WIDTH = 500;
    private String username;
    private final JTextField text;
    private boolean registered;
    private final ConnectionHandler connectionHandler;

    public TitleScreenPanel(ConnectionHandler connectionHandler) {
        int HEIGHT = 800;
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setLayout(null);
        var label = new JLabel("Enter a username");
        label.setFont(new Font(getFont().getFontName(), getFont().getStyle(), 25));
        var bounds = getFontMetrics(label.getFont()).getStringBounds("Enter a username:", getGraphics());
        label.setBounds(WIDTH / 2 - (int) bounds.getWidth() / 2, 300, (int) bounds.getWidth(), (int) bounds.getHeight());
        add(label);
        text = new JTextField();
        text.setFont(new Font(getFont().getFontName(), getFont().getStyle(), 15));
        text.setBounds(label.getX(), label.getY() + 60, (int) bounds.getWidth(), 40);
        text.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER)
                    submit();
            }
        });
        add(text);
        var submitBtn = new JButton("PLAY!");
        var btnBounds = new Rectangle(label.getX(), text.getY() + 90, (int) bounds.getWidth(), (int) bounds.getWidth());
        btnBounds.grow(20, 20);
        submitBtn.setBounds(btnBounds);
        submitBtn.setFont(new Font(getFont().getFontName(), getFont().getStyle(), 48));
        submitBtn.addActionListener(_ -> submit());
        submitBtn.setFocusable(true);
        add(submitBtn);
        this.connectionHandler = connectionHandler;
        connectionHandler.addObserver(this);
    }

    private void submit() {
        if (!text.getText().isBlank())
            setUsername(text.getText());
    }

    @Override
    protected void paintComponent(Graphics g) {
        g.setFont(new Font(getFont().getFontName(), Font.ITALIC, 50));
        var titleString = "duel.io";
        var bounds = g.getFontMetrics().getStringBounds(titleString, g);
        var width = (int) bounds.getWidth();
        g.drawString(titleString, WIDTH / 2 - width / 2, 230);
    }

    private void setUsername(String username) {
        this.username = username;
    }

    public Map<String, String> getInput() {
        final var POLL_RATE = Duration.ofMillis(100);
        var returnUsername = (String) null;
        while (!registered) {
            if (username != null) {
                if (connectionHandler.getConnectionStatus() != ConnectionHandler.ConnectionStatus.SUCCESS) {
                    showMessageDialog(this, "Server is not running currently", "Error", JOptionPane.ERROR_MESSAGE);
                } else {
                    sendNewPlayer();
                    returnUsername = username;
                    username = null;
                }
            }
            try {
                Thread.sleep(POLL_RATE);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        if (returnUsername == null)
            throw new IllegalStateException("Username cannot be null");
        return Map.of("username", returnUsername);
    }

    private void sendNewPlayer() {
        connectionHandler.sendMessage(Map.of(
                "request_type", "new-player",
                "data", Map.of(
                        "username", username
                )
        ));
    }

    @Override
    public void handleMessage(Map<String, Object> message) {
        var requestType = (String) message.get("request_type");
        if (requestType.equals("new-player-success")) {
            registered = true;
        } else if (requestType.equals("new-player-error")) {
            showMessageDialog(this, "Error: " + message.get("message"), "Error", JOptionPane.ERROR_MESSAGE);
            username = null;
            text.setText("");
        }
    }
}
