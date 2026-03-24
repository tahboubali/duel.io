package org.example;

import com.google.gson.annotations.Expose;

import java.awt.*;
import java.awt.geom.Line2D;
import java.util.HashSet;
import java.util.UUID;

import static java.lang.Math.abs;
import static java.lang.Math.cos;
import static java.lang.Math.hypot;
import static java.lang.Math.round;
import static java.lang.Math.sin;
import static java.lang.Math.toDegrees;
import static java.lang.Math.toRadians;
import static org.example.PolyUtils.from;
import static org.example.PolyUtils.getCorners;
import static org.example.PolyUtils.getHeight;
import static org.example.PolyUtils.getWidth;
import static org.example.Wall.DOWN;
import static org.example.Wall.LEFT;

public class Projectile implements PhysicsObject {
    private static final double WALL_DAMPING = .44;
    private static final double OBJECT_DAMPING = .6;
    @Expose
    private Vec2 position;
    @Expose
    private Vec2 velocity;
    private static final double INIT_SPEED = 1.4;
    public static final int PROJ_WIDTH = 30;
    public static final int PROJ_HEIGHT = 14;
    @Expose
    private double angle;
    private static final int DESPAWN_TIME = 1000;
    private long timeSettled;
    private PhysicsHandler.GravityApplier gravityApplier;
    private long lastCollision;
    private final Player player;
    private boolean destroy;
    @Expose
    private String id;

    public Projectile(double x, double y, Vec2 direction, Player player) {
        int range = 10;
        int min = -5;
        double recoil = (Math.random() * range + 1) + min;
        this.velocity = direction.mul(INIT_SPEED);
        this.angle = toRadians(toDegrees(direction.asAngle()) + recoil);
        this.velocity.set(cos(angle), sin(angle));
        this.position = Vec2.of(x, y);
        this.player = player;
        id = UUID.randomUUID().toString();
    }

    @SuppressWarnings("unused")
    public Projectile() {
        player = null;
    }

    @Override
    public void update(double dt) {
        if (timeSettled == 0 && abs(velocity.getX()) <= 0.0001 && abs(velocity.getY()) <= .00001) {
            timeSettled = System.currentTimeMillis();
        }
    }

    @Override
    public void draw(Graphics2D g2d) {
        double centerX = getX() + PROJ_WIDTH / 2d;
        double centerY = getY() + PROJ_HEIGHT / 2d;
        java.awt.geom.AffineTransform transform = g2d.getTransform();
        g2d.setColor(player != null ? Color.BLUE : Color.RED.brighter());
        g2d.rotate(angle, centerX, centerY);
        g2d.fillRect((int) round(position.getX()), (int) round(position.getY()), PROJ_WIDTH, PROJ_HEIGHT);
        g2d.setColor(Color.GREEN);
        g2d.fillOval((int) centerX - 5, (int) centerY - 5, 10, 10);
        g2d.setTransform(transform);
        g2d.setColor(player != null ? Color.RED.brighter() : new Color(93, 0, 0));
        g2d.setStroke(new BasicStroke(2));
        g2d.drawPolygon(getCollisionPoly());
    }

    public boolean shouldDespawn() {
        return destroy || (timeSettled != 0 && System.currentTimeMillis() - timeSettled >= DESPAWN_TIME);
    }

    private Rectangle getFittedBox() {
        double cw = PROJ_WIDTH * abs(cos(angle)) + PROJ_HEIGHT * abs(sin(angle));
        double ch = PROJ_WIDTH * abs(sin(angle)) + PROJ_HEIGHT * abs(cos(angle));
        return new Rectangle(
                (int) round((getX() + PROJ_WIDTH / 2d) - cw / 2),
                (int) round((getY() + PROJ_HEIGHT / 2d) - ch / 2),
                (int) round(cw),
                (int) round(ch)
        );
    }

    @Override
    public double getX() {
        return position.getX();
    }

    @Override
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
        this.position.set(vec2);
    }

    @Override
    public Vec2 getPosition() {
        return position;
    }

    @Override
    public Polygon getCollisionPoly() {
        return from(
                new Rectangle(round((float) position.getX()), round((float) position.getY()), PROJ_WIDTH, PROJ_HEIGHT),
                angle
        );
    }

    @Override
    public double getMass() {
        return .06;
    }

    @Override
    public void handleObjectCollision(PhysicsObject obj) {
        if (obj instanceof Block) {
            Rectangle intersection = getFittedBox().intersection(obj.getCollisionPoly().getBounds());
            Polygon poly = obj.getCollisionPoly();
            HashSet<Line2D> lines = new HashSet<Line2D>();
            Point[] corners = getCorners(poly);
            for (Point corner : corners) {
                for (Point other : corners) {
                    if (corner.distance(other) < hypot(getWidth(poly), getHeight(poly))) {
                        lines.add(new Line2D.Double(corner, other));
                    }
                }
            }

            for (Line2D line : lines) {
                if (intersection.intersectsLine(line)) {
                    if (line.getP1().getX() == line.getP2().getX()) {
                        bounce(OBJECT_DAMPING, LEFT);
                    } else {
                        bounce(OBJECT_DAMPING, DOWN);
                    }
                    return;
                }
            }
        } else if (obj instanceof Player && player != obj) {
            destroy();
        }
    }

    @Override
    public double getAngle() {
        return angle;
    }

    @Override
    public void handleWallCollision(Wall... walls) {
        bounce(WALL_DAMPING, walls);
    }

    @Override
    public void setAngle(double angle) {
        if (player == null) {
            return;
        }
        this.angle = angle;
    }

    public Vec2 getVelocity() {
        if (player == null) {
            return Vec2.zero();
        }
        return velocity;
    }

    public Vec2 getDamageVelocity() {
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

    public void destroy() {
        destroy = true;
    }

    public String getId() {
        return id;
    }
}
