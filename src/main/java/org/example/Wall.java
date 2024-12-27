package org.example;

import static org.example.Vec2.*;

public enum Wall {
    UP,
    LEFT,
    DOWN,
    RIGHT;

    public Vec2 normal() {
        return switch (this) {
            case UP -> up();
            case LEFT -> left();
            case DOWN -> down();
            case RIGHT -> right();
        };
    }
}