package org.example;

import static org.example.Vec2.down;
import static org.example.Vec2.left;
import static org.example.Vec2.right;
import static org.example.Vec2.up;

public enum Wall {
    UP,
    LEFT,
    DOWN,
    RIGHT;

    public Vec2 normal() {
        switch (this) {
            case UP:
                return up();
            case LEFT:
                return left();
            case DOWN:
                return down();
            case RIGHT:
                return right();
            default:
                throw new IllegalStateException("Unhandled wall: " + this);
        }
    }
}
