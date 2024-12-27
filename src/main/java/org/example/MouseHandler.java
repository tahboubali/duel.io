package org.example;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class MouseHandler extends MouseAdapter {
    private static final MouseHandler mouseHandler = new MouseHandler();
    private boolean leftClicked, rightClicked;

    public static MouseHandler getInstance() {
        return mouseHandler;
    }

    @Override
    public void mousePressed(MouseEvent e) {
        setButtons(e, true);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        setButtons(e, false);
    }

    private void setButtons(MouseEvent e, boolean pressed) {
        if (e.getButton() == MouseEvent.BUTTON1) {
            leftClicked = pressed;
        }
        if (e.getButton() == MouseEvent.BUTTON3) {
            rightClicked = pressed;
        }
    }

    public static boolean isRightClicked() {
        return mouseHandler.rightClicked;
    }

    public static boolean isLeftClicked() {
        return mouseHandler.leftClicked;
    }
}
