package org.example;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

public class TitleScreenPanel extends JPanel {
    private final int WIDTH = 500, HEIGHT = 800;
    private String username;


    public TitleScreenPanel() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setLayout(null);
        var label = new JLabel("Enter a username");
        label.setFont(new Font(getFont().getFontName(), getFont().getStyle(), 25));
        var bounds = getFontMetrics(label.getFont()).getStringBounds("Enter a username:", getGraphics());
        label.setBounds(500 / 2 - (int) bounds.getWidth() / 2, 300, (int) bounds.getWidth(), (int) bounds.getHeight());
        add(label);
        var text = new JTextField();
        text.setFont(new Font(getFont().getFontName(), getFont().getStyle(), 15));
        text.setBounds(label.getX(), label.getY() + 60, (int) bounds.getWidth(), 40);
        add(text);
        var submitBtn = new JButton("PLAY!");
        var btnBounds = new Rectangle(label.getX(), text.getY() + 90, (int) bounds.getWidth(), (int) bounds.getWidth());
        btnBounds.grow(20, 20);
        submitBtn.setBounds(btnBounds);
        submitBtn.setFont(new Font(getFont().getFontName(), getFont().getStyle(), 48));
        submitBtn.addActionListener(_ -> {
            setUsername(text.getText());
            System.out.println("clicked!");
            System.out.println(text.getText());
        });
        submitBtn.setFocusable(true);
        add(submitBtn);
    }

    @Override
    protected void paintComponent(Graphics g) {
        g.setFont(new Font(getFont().getFontName(), getFont().getStyle(), 50));
        var titleString = "Duel Game";
        var bounds = g.getFontMetrics().getStringBounds(titleString, g);
        var width = (int) bounds.getWidth();
        g.drawString(titleString, WIDTH / 2 - width / 2, 240);
    }

    private void setUsername(String username) {
        this.username = username;
    }

    private String getUsername() {return username;}

    public Map<String, String> getInput() {
        while (getUsername() == null) {
            System.out.println("hi");getUsername();}
        return Map.of("username", getUsername());
    }
}
