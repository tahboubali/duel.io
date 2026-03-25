package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.time.Duration;
import java.util.Map;

import static javax.swing.JOptionPane.showMessageDialog;

public class TitleScreen extends JPanel implements ConnectionHandler.MessageObserver {
    private final int WIDTH = 500;
    private final boolean autoPlayEnabled;
    private final String autoUsername;
    private String username;
    private final JTextField text;
    private boolean registered;
    private boolean registrationPending;
    private boolean autoPlayAttempted;
    private final ConnectionHandler connectionHandler;

    public TitleScreen(ConnectionHandler connectionHandler) {
        int HEIGHT = 800;
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setLayout(null);
        JLabel label = new JLabel("Enter a username");
        label.setFont(new Font(getFont().getFontName(), getFont().getStyle(), 25));
        Rectangle bounds = getFontMetrics(label.getFont()).getStringBounds("Enter a username:", getGraphics()).getBounds();
        label.setBounds(WIDTH / 2 - (int) bounds.getWidth() / 2, 300, (int) bounds.getWidth(), (int) bounds.getHeight());
        add(label);
        text = new JTextField();
        text.setFont(new Font(getFont().getFontName(), getFont().getStyle(), 15));
        text.setBounds(label.getX(), label.getY() + 60, (int) bounds.getWidth(), 40);
        text.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    submit();
                }
            }
        });
        add(text);
        JButton submitBtn = new JButton("PLAY!");
        Rectangle btnBounds = new Rectangle(label.getX(), text.getY() + 90, (int) bounds.getWidth(), (int) bounds.getWidth());
        btnBounds.grow(20, 20);
        submitBtn.setBounds(btnBounds);
        submitBtn.setFont(new Font(getFont().getFontName(), getFont().getStyle(), 48));
        submitBtn.addActionListener(e -> submit());
        submitBtn.setFocusable(true);
        add(submitBtn);
        this.connectionHandler = connectionHandler;
        this.autoPlayEnabled = BrowserHarnessBridge.isEnabled("autoplay");
        this.autoUsername = BrowserHarnessBridge.getQueryParam("username");
        connectionHandler.addObserver(this);
        setBackground(Color.WHITE);
    }

    private void submit() {
        if (!text.getText().trim().isEmpty()) {
            setUsername(text.getText());
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setFont(new Font(getFont().getFontName(), Font.ITALIC, 50));
        String titleString = "duel.io";
        Rectangle bounds = g.getFontMetrics().getStringBounds(titleString, g).getBounds();
        int width = (int) bounds.getWidth();
        g.drawString(titleString, WIDTH / 2 - width / 2, 230);
    }

    private void setUsername(String username) {
        this.username = username;
        this.registrationPending = false;
    }

    public Map<String, String> getInput() {
        final Duration pollRate = Duration.ofMillis(100);
        String returnUsername = null;
        while (!registered) {
            if (autoPlayEnabled && !autoPlayAttempted && username == null && autoUsername != null && !autoUsername.trim().isEmpty()) {
                autoPlayAttempted = true;
                text.setText(autoUsername);
                setUsername(autoUsername);
                BrowserHarnessBridge.reportStatus("auto submit username " + autoUsername);
            }

            if (username != null && !registrationPending) {
                if (connectionHandler.getConnectionStatus() != ConnectionHandler.ConnectionStatus.SUCCESS) {
                    if (!autoPlayEnabled) {
                        showMessageDialog(this, "Server is not running currently", "Error", JOptionPane.ERROR_MESSAGE);
                        username = null;
                    }
                } else {
                    sendNewPlayer();
                    registrationPending = true;
                    returnUsername = username;
                }
            }
            try {
                Thread.sleep(pollRate.toMillis());
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        if (returnUsername == null) {
            throw new IllegalStateException("Username cannot be null");
        }
        return Maps.of("username", returnUsername);
    }

    private void sendNewPlayer() {
        connectionHandler.sendMessage(Maps.of(
                "request_type", "new-player",
                "data", Maps.of(
                        "username", username
                )
        ));
    }

    @Override
    public void handleMessage(Map<String, Object> message) {
        String requestType = (String) message.get("request_type");
        if (requestType.equals("new-player-success")) {
            registered = true;
        } else if (requestType.equals("new-player-error")) {
            showMessageDialog(this, "Error: " + message.get("message"), "Error", JOptionPane.ERROR_MESSAGE);
            username = null;
            registrationPending = false;
        }
    }
}
