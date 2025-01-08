package org.example;

import java.awt.*;
import java.time.Duration;

import static java.lang.Math.round;
import static java.lang.System.currentTimeMillis;
import static org.example.PolyUtils.from;
import static org.example.PhysicsHandler.GravityApplier;
import static org.example.PolyUtils.getCorners;

public class Block implements PhysicsObject {
    private Color color;
    private final Vec2 position;
    public static final int WIDTH = (int) round(40 * 1.76), HEIGHT = (int) round(40 * 1.76);
    private static final Duration DESPAWN_TIME = Duration.ofSeconds(60);
    private final long createdMillis;
    private GravityApplier gravityApplier;
    private long lastCollision;
    private final Vec2 velocity;
    private final Player player;

    public Block(double x, double y, Color color, Player player) {
        this.color = color;
        position = Vec2.of(x, y);
        createdMillis = currentTimeMillis();
        velocity = Vec2.zero();
        this.player = player;
    }

    @Override
    public void update(double dt) {
        position.set(position.add(velocity));
    }

    @Override
    public void draw(Graphics2D g2d) {
        var point = position.asPoint();
        g2d.setColor(color);
        g2d.fillRect(point.x, point.y, WIDTH, HEIGHT);
        var corners = getCorners(getCollisionPoly());
        int rad = 3;
        g2d.setColor(Color.RED);
        for (var corner : corners) {
            g2d.fillOval(corner.x - rad, corner.y - rad, rad * 2, rad * 2);
        }
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
        return from(new Rectangle(round((float) position.getX()), round((float) position.getY()), WIDTH, HEIGHT));
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
        if (obj instanceof Projectile) {
            double factor = .9;
            color = new Color((int) round(color.getRed() * factor),
                    (int) round(color.getGreen() * factor),
                    (int) round(color.getBlue() * factor));
            return;
        }
        if (obj instanceof Player) {
            return;
        }
        var velocity = getGravityApplier().getPrevVelocity();
        var objVelocity = obj.getGravityApplier().getPrevVelocity();
        double OBJECT_DAMPING = .8;
        var thisVf = (velocity.mul((this.getMass() - OBJECT_DAMPING * obj.getMass())).add(objVelocity.mul((1 + OBJECT_DAMPING) * obj.getMass()))).div((this.getMass() + obj.getMass()));
        this.getGravityApplier().getGravityVelocity().set(thisVf);
        if (this.position.getY() < obj.getPosition().getY())
            bounce(.2, Wall.DOWN);
    }

    @Override
    public double getAngle() {
        return 0;
    }

    @Override
    public void handleWallCollision(Wall... walls) {
        bounce(.2, walls);
    }

    @Override
    public void setAngle(double angle) {

    }

    public Vec2 getVelocity() {
        return velocity;
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

    @Override
    public void setLastCollision(long timeNanos) {
        this.lastCollision = timeNanos;
    }

    @Override
    public long lastCollision() {
        return lastCollision;
    }

    public Player getPlayer() {
        return player;
    }
}
