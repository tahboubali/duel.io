package org.example;

import java.awt.*;

import static java.util.Arrays.stream;

public interface PhysicsObject extends GameObj {
    double getX();

    double getY();

    void setX(double x);

    void setY(double y);

    void setPosition(Vec2 vec2);

    Vec2 getPosition();

    Polygon getCollisionPoly();

    double getMass();

    void handleWallCollision(Wall... walls);

    void handleObjectCollision(PhysicsObject physObj);

    double getAngle();

    void setAngle(double angle);

    Vec2 getVelocity();

    boolean hasCollision();

    default boolean tracked() {
        return true;
    }

    default void bounce(double damping, Wall... walls) {
        stream(walls).forEach(wall -> {
            var velocity = getVelocity();
            var gravityV = getGravityApplier().getGravityVelocity();
            var prev = getGravityApplier().getPrevVelocity();
            switch (wall) {
                case UP, DOWN -> {
                    velocity.set(velocity.mul(Vec2.of(damping, -(damping - .1))));
                    gravityV.set(prev.mul(Vec2.of(damping, -(damping - .05))));
                }
                case LEFT, RIGHT -> velocity.set(velocity.mul(Vec2.of(-(damping - .1), damping)));
            }
        });
    }

    void setGravityApplier(PhysicsHandler.GravityApplier applier);

    PhysicsHandler.GravityApplier getGravityApplier();

    long lastCollision();

    void setLastCollision(long timeNanos);
}
