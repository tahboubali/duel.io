package org.example;

import com.google.gson.annotations.Expose;

import java.awt.*;
import java.time.Duration;

import static java.lang.Math.*;
import static java.lang.System.currentTimeMillis;
import static org.example.PolyUtils.from;
import static org.example.PhysicsHandler.GravityApplier;

public class Block implements PhysicsObject {
    private final static int MAX_HEALTH = 10;
    private Color color;
    @Expose
    private Vec2 position;
    public static final int WIDTH = (int) round(40 * 1.76), HEIGHT = (int) round(40 * 1.76);
    private static final long DESPAWN_TIME = Duration.ofSeconds(40).toMillis();
    private final long createdMillis;
    private GravityApplier gravityApplier;
    private long lastCollision;
    private final Vec2 velocity;
    private final Player player;
    @Expose
    private double health;
    private final GamePanel gamePanel;
    private boolean destroy;

    public Block(double x, double y, Color color, Player player, GamePanel gamePanel) {
        this.color = color;
        position = Vec2.of(x, y);
        createdMillis = currentTimeMillis();
        velocity = Vec2.zero();
        this.player = player;
        health = MAX_HEALTH;
        this.gamePanel = gamePanel;
    }

    // used for Json serialization/deserialization
    public Block() {
        createdMillis = currentTimeMillis();
        velocity = Vec2.zero();
        this.player = null;
        this.gamePanel = null;
        color = Color.RED;
    }

    @Override
    public void update(double dt) {
        double healthDec = dt / DESPAWN_TIME * MAX_HEALTH;
        health -= healthDec;
        position.set(position.add(velocity));
    }

    @Override
    public void draw(Graphics2D g2d) {
        var point = position.asPoint();
        double r = max(0, color.getRed() - (health / MAX_HEALTH * 255));
        double g = max(0, color.getGreen() - (health / MAX_HEALTH * 255));
        double b = max(0, color.getBlue() - (health / MAX_HEALTH * 255));
        color = new Color(max(0, (int) round(color.getRed() - r)), max(0, (int) round(color.getGreen() - g)), max(0, (int) round(color.getBlue() - b)));
        g2d.setColor(color);
        g2d.fillRect(point.x, point.y, WIDTH, HEIGHT);
        g2d.setColor(Color.WHITE);
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRect(point.x, point.y, WIDTH, HEIGHT);
        g2d.setColor(Color.BLACK);
    }

    public double getX() {
        return position.getX();
    }

    public double getY() {
        return position.getY();
    }

    @Override
    public void setX(double x) {
        setPosition(Vec2.of(x, getY()));
    }

    @Override
    public void setY(double y) {
        setPosition(Vec2.of(getX(), y));
    }

    @Override
    public void setPosition(Vec2 vec2) {
        position.set(Vec2.of(
                max(0, min(vec2.getX(), Main.getGamePanel().getWidth() - WIDTH + .5)),
                max(0, min(vec2.getY(), Main.getGamePanel().getHeight() - HEIGHT + .5))
        ));
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
        return destroy || currentTimeMillis() - createdMillis >= DESPAWN_TIME || health <= 0;
    }

    @Override
    public double getMass() {
        return .35;
    }

    @Override
    public void handleObjectCollision(PhysicsObject obj) {
        if (obj instanceof Projectile projectile) {
            double damage = projectile.getDamageVelocity().magnitude();
            health -= damage;
        }
        if (this.getCollisionPoly().getBounds().getCenterY() < obj.getCollisionPoly().getBounds().getCenterY())
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

    public void destroy() {
        destroy = true;
    }

    public Player getPlayer() {
        return player;
    }
}
