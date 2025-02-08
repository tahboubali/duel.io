package org.example;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import static java.awt.event.KeyEvent.*;

public class KeyHandler extends KeyAdapter {
    private boolean up, shift, left, right, space, hide;
    private static final KeyHandler KEY_HANDLER = new KeyHandler();

    private KeyHandler() {
    }

    @Override
    public void keyPressed(KeyEvent e) {
        setDirections(e.getKeyCode(), true);
        if (e.getKeyCode() == KeyEvent.VK_SPACE) space = true;
    }

    @Override
    public void keyReleased(KeyEvent e) {
        setDirections(e.getKeyCode(), false);
        if (e.getKeyCode() == KeyEvent.VK_SPACE) space = false;
        if (e.getKeyCode() == KeyEvent.VK_H) hide = !hide;
    }

    private void setDirections(int code, boolean active) {
        if (code == VK_W || code == VK_UP)
            up = active;
        if (code == VK_A || code == VK_LEFT)
            left = active;
        if (code == VK_SHIFT)
            shift = active;
        if (code == VK_D || code == VK_RIGHT)
            right = active;
    }

    public static KeyHandler getInstance() {
        return KEY_HANDLER;
    }

    public static boolean isUp() {
        return KEY_HANDLER.up;
    }

    public static boolean isLeft() {
        return KEY_HANDLER.left;
    }

    public static boolean isShift() {
        return KEY_HANDLER.shift;
    }

    public static boolean isRight() {
        return KEY_HANDLER.right;
    }

    public static boolean isSpace() {
        return KEY_HANDLER.space;
    }

    public static boolean isHide() {
        return KEY_HANDLER.hide;
    }
}
