package org.example;

import java.awt.*;
import java.time.Duration;

import static java.lang.Math.round;
import static java.lang.System.currentTimeMillis;
import static org.example.PhysicsHandler.GravityApplier;

public class Block implements PhysicsObject {
    private Color color;
    private final Vec2 position;
    private static final int WIDTH = 40 * 2, HEIGHT = 40 * 2;
    private static final Duration DESPAWN_TIME = Duration.ofSeconds(60);
    private final long createdMillis;
    private GravityApplier gravityApplier;

    public Block(double x, double y, Color color) {
        this.color = color;
        position = Vec2.of(x, y);
        createdMillis = currentTimeMillis();
    }

    @Override
    public void update(double dt) {

    }

    @Override
    public void draw(Graphics2D g2d) {
        var point = position.asPoint();
        g2d.setColor(color);
        g2d.fillRect(point.x, point.y, WIDTH, HEIGHT);
    }

    public double getX() {
        return position.getX();
    }

    public double getY() {
        return position.getY();
    }

    @Override
    public void setX(double x) {
        this.position.setX(x);
    }

    @Override
    public void setY(double y) {
        this.position.setY(y);
    }

    @Override
    public void setPosition(Vec2 vec2) {
        position.set(vec2);
    }

    @Override
    public Vec2 getPosition() {
        return position;
    }

    @Override
    public Polygon getCollisionPoly() {
        return new Rectangle(round((float) position.getX() + 2), round((float) position.getY()), WIDTH - 4, HEIGHT);
    }

    public boolean shouldDespawn() {
        return currentTimeMillis() - createdMillis >= DESPAWN_TIME.toMillis();
    }

    @Override
    public double getMass() {
        return .35;
    }

    @Override
    public void handleObjectCollision(PhysicsObject obj) {
        double factor = .9;
        color = new Color((int) round(color.getRed() * factor),
                (int) round(color.getGreen() * factor),
                (int) round(color.getBlue() * factor));
        if (obj instanceof Projectile)
            return;
        var velocity = getGravityApplier().getPrevVelocity();
        var objVelocity = obj.getGravityApplier().getPrevVelocity();
        double OBJECT_DAMPING = .8;
        var thisVf = (velocity.mul((this.getMass() - OBJECT_DAMPING * obj.getMass())).add(objVelocity.mul((1 + OBJECT_DAMPING) * obj.getMass()))).div((this.getMass() + obj.getMass()));
        this.getGravityApplier().getGravityVelocity().set(thisVf);
        if (this.position.getY() < obj.getPosition().getY())
            bounce(.2, Wall.DOWN);
    }

    @Override
    public void handleWallCollision(Wall... walls) {
        bounce(.2, walls);
    }

    @Override
    public void setAngle(double angle) {

    }

    public Vec2 initDirection() {
        return Vec2.zero();
    }

    public Vec2 getVelocity() {
        return Vec2.zero();
    }

    @Override
    public boolean hasCollision() {
        return true;
    }

    @Override
    public boolean tracked() {
        return !shouldDespawn();
    }

    @Override
    public void setGravityApplier(PhysicsHandler.GravityApplier applier) {
        this.gravityApplier = applier;
    }

    @Override
    public PhysicsHandler.GravityApplier getGravityApplier() {
        return gravityApplier;
    }
}
