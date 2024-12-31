package org.example;

import com.google.gson.annotations.Expose;

import java.awt.*;
import java.awt.geom.Line2D;
import java.util.HashSet;

import static java.lang.Math.*;
import static org.example.PolyUtils.*;
import static org.example.Wall.*;

public class Projectile implements PhysicsObject {
    private final static double WALL_DAMPING = .44, OBJECT_DAMPING = .6;
    @Expose
    private final Vec2 position;
    private final Vec2 velocity;
    private static final double INIT_SPEED = 1.4;
    public static final int PROJ_WIDTH = 30, PROJ_HEIGHT = 14;
    @Expose
    private double angle;
    private final GamePanel gamePanel;
    private final Vec2 initDirection;
    private static final int DESPAWN_TIME = 1000;
    private long timeSettled;
    private PhysicsHandler.GravityApplier gravityApplier;

    public Projectile(GamePanel gamePanel, double x, double y, Vec2 direction) {
        int range = 10;
        int min = -5;
        var recoil = (Math.random() * range + 1) + min;
        this.velocity = direction.mul(INIT_SPEED);
        this.angle = toRadians(toDegrees(direction.asAngle()) + recoil);
        this.velocity.set(cos(angle), sin(angle));
        this.position = Vec2.of(x, y);
        this.gamePanel = gamePanel;
        this.initDirection = direction;
    }

    @Override
    public void update(double dt) {
        if (timeSettled == 0 && abs(velocity.getX()) <= 0.0001 && abs(velocity.getY()) <= .00001)
            timeSettled = System.currentTimeMillis();
    }

    @Override
    public void draw(Graphics2D g2d) {
        double centerX = getX() + PROJ_WIDTH / 2d;
        double centerY = getY() + PROJ_HEIGHT / 2d;
//        g2d.setColor(Color.BLACK);
//        g2d.draw(getFittedBox());
        var transform = g2d.getTransform();
        g2d.setColor(Color.BLUE);
        g2d.rotate(angle, centerX, centerY);
        g2d.fillRect((int) round(position.getX()), (int) round(position.getY()), PROJ_WIDTH, PROJ_HEIGHT);
        g2d.setColor(Color.GREEN);
        g2d.fillOval((int) centerX - 5, (int) centerY - 5, 10, 10);
        g2d.setTransform(transform);
    }

    private boolean outOfBounds() {
        return !(gamePanel.getBounds().contains(this.getCollisionPoly().getBounds()));
    }

    public boolean shouldDespawn() {
        return outOfBounds() || (timeSettled != 0 && System.currentTimeMillis() - timeSettled >= DESPAWN_TIME);
    }

    private Rectangle getFittedBox() {
        double cw = PROJ_WIDTH * abs(cos(angle)) + PROJ_HEIGHT * abs(sin(angle));
        double ch = PROJ_WIDTH * abs(sin(angle)) + PROJ_HEIGHT * abs(cos(angle));
        return new Rectangle(
                (int) round((getX() + PROJ_WIDTH / 2d) - cw / 2), (int) round((getY() + PROJ_HEIGHT / 2d) - ch / 2),
                (int) round(cw), (int) round(ch)
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
        System.out.println(angle);
        return from(
                new Rectangle(
                        round((float) position.getX()), round((float) position.getY()), PROJ_WIDTH, PROJ_HEIGHT
                ),
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
            var intersection = getFittedBox().intersection(obj.getCollisionPoly().getBounds());
            var poly = obj.getCollisionPoly();
            // get lines
            var lines = new HashSet<Line2D>();
            var corners = getCorners(poly);
            for (var corner : corners) {
                for (var other : corners) {
                    if (corner.distance(other) < hypot(getWidth(poly), getHeight(poly)))
                        lines.add(new Line2D.Double(corner, other));
                }
            }
            for (var line : lines) {
                if (intersection.intersectsLine(line)) {
                    if (line.getP1().getX() == line.getP2().getX()) {
                        bounce(OBJECT_DAMPING, LEFT);
                    } else {
                        bounce(OBJECT_DAMPING, DOWN);
                    }
                    return;
                }
            }
        }
    }

    @Override
    public void handleWallCollision(Wall... walls) {
        bounce(WALL_DAMPING, walls);
    }

    @Override
    public void setAngle(double angle) {
        this.angle = angle;
    }

    public Vec2 initDirection() {
        return initDirection;
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
}
