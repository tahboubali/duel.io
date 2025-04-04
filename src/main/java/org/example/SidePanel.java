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
        duelButton.setBounds(20, 50, 120, 30);
        add(duelButton);
        duelButton.addActionListener(_ -> {
            onDuelButtonClick();
            requestFocusInWindow();
        });
        addKeyListener(KeyHandler.getInstance());
        setFocusable(true);
    }

    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(Color.WHITE);
        g.drawString("Options", 20, 30);
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
