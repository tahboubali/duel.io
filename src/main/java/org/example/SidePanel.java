package org.example;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

import static org.example.ConnectionHandler.MessageObserver;

public class SidePanel extends JPanel implements MessageObserver {
    private final ConnectionHandler connectionHandler;

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
        drawConnectionStatus(g, 20, 60);
    }

    private void drawConnectionStatus(Graphics g, int x, int y) {
        var label = "Connection: ";
        var status = connectionHandler.getConnectionStatus();
        g.setColor(Color.WHITE);
        g.drawString(label, x, y);
        var fm = g.getFontMetrics();
        int labelWidth = fm.stringWidth(label);
        g.setColor(switch (status) {
            case SUCCESS -> Color.GREEN;
            case CONNECTING -> Color.ORANGE;
            case FAILED -> Color.RED;
        });
        g.drawString(status.toString(), x + labelWidth, y);
    }

    public void onDuelButtonClick() {
        connectionHandler.sendMessage(Map.of(
                "request_type", "enter-duel"
        ));
    }

    @Override
    public void handleMessage(Map<String, Object> message) {
        System.out.println(message);
    }
}
