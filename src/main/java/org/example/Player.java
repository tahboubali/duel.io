package org.example;

import java.awt.*;

import static java.lang.Math.round;
import static org.example.KeyHandler.*;
import static org.example.MouseHandler.isLeftClicked;
import static org.example.MouseHandler.isRightClicked;
import static org.example.ConnectionHandler.*;

import java.util.ArrayList;
import java.util.List;

public class Player implements PhysicsObject {
    public static final int WIDTH = 30;
    public static final int HEIGHT = 66;
    private static final int SHOT_COOLDOWN_MILLIS = 320,
            BUILD_COOLDOWN_MILLIS = 100; // 350
    private final static double SPEED = .5;
    private final static double JUMP_VELOCITY = 1.4;
    private final Shooter shooter;
    private Vec2 position;
    private long lastShot, lastBuilt;
    private final List<Block> blocks;
    private boolean jumping;
    private final GamePanel gamePanel;
    private Vec2 velocity;
    private final Vec2 combinedVelocity = Vec2.zero();
    private final String name;
    private PhysicsHandler.GravityApplier gravityApplier;

    public Player(GamePanel gamePanel, String name) {
        this.gamePanel = gamePanel;
        this.position = Vec2.of(gamePanel.getPreferredSize().getWidth() / 2d - WIDTH / 2d, gamePanel.getPreferredSize().getHeight() / 2d - HEIGHT / 2d);
        this.shooter = new Shooter(this, gamePanel);
        this.blocks = new ArrayList<>();
        this.velocity = Vec2.zero();
        this.name = name;
    }

    @Override
    public void update(double dt) {
        velocity = Vec2.zero();
        if (isLeft()) {
            velocity.setX(velocity.getX() - SPEED);
        }
        if (isDown()) {
            velocity.setY(velocity.getY() + SPEED);
        }
        if (isRight()) {
            velocity.setX(velocity.getX() + SPEED);
        }
        if (isSpace()) {
            jumping = true;
        }
        if (isLeftClicked()) {
            long now = System.currentTimeMillis();
            if (now - lastShot >= SHOT_COOLDOWN_MILLIS) {
                shooter.shoot();
                lastShot = now;
            }
        }
        if (isRightClicked()) {
            var block = new Block(position.getX(), position.getY(), Color.GREEN);
            var intersects = false;
            for (var other : blocks.stream().map(Block::getCollision).toList()) {
                if (block.getCollision().intersects(other)) {
                    intersects = true;
                    break;
                }
            }
            long now = System.currentTimeMillis();
            if (now - lastBuilt >= BUILD_COOLDOWN_MILLIS && !intersects) {
                gamePanel.addPhysicsObject(block);
                blocks.add(block);
                lastBuilt = now;
            }
        }
        if (jumping) {
            if (position.getY() + HEIGHT >= gamePanel.getSize().height) {
                jumping = false;
            }
            velocity.setY(-JUMP_VELOCITY);
        }
        shooter.update(dt);
        synchronized (blocks) {
            blocks.forEach(block -> block.update(dt));
            blocks.removeIf(Block::shouldDespawn);
        }
    }

    @Override
    public void draw(Graphics2D g2d) {
        g2d.setColor(Color.BLUE);
        var point = position.asPoint();
        g2d.fillRect(point.x, point.y, WIDTH, HEIGHT);
        var fontMetrics = g2d.getFontMetrics();
        int usernameWidth = fontMetrics.stringWidth(name);
        int offset = fontMetrics.getHeight() + 20;
        int nameX = point.x - usernameWidth / 2 + WIDTH / 2;
        int nameY = (point.y - offset) - fontMetrics.getHeight() / 2 + HEIGHT / 2;
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(5f));
        g2d.drawString(name, nameX, nameY);
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(2.3f));
        g2d.drawRect(point.x, point.y, WIDTH, HEIGHT);
        shooter.draw(g2d);
        synchronized (blocks) {
            blocks.forEach(block -> block.draw(g2d));
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
        position.setX(x);
    }

    @Override
    public void setY(double y) {
        position.setY(y);
    }

    @Override
    public void setPosition(Vec2 vec2) {
        position = vec2;
    }

    @Override
    public Vec2 getPosition() {
        return position;
    }

    @Override
    public Rectangle getCollision() {
        return new Rectangle(round((float) position.getX()), round((float) position.getY()), WIDTH, HEIGHT);
    }

    @Override
    public double getMass() {
        return 1;
    }

    @Override
    public void handleObjectCollision(PhysicsObject physObj) {

    }

    @Override
    public void handleWallCollision(Wall... walls) {

    }

    @Override
    public void setAngle(double angle) {}

    @Override
    public Vec2 initDirection() {
        return Vec2.zero();
    }

    public Vec2 getVelocity() {
        return velocity;
    }

    public String getName() {
        return name;
    }

    public PlayerUpdateInfo getUpdateInfo() {
        return new PlayerUpdateInfo(round((float) getX()), round((float) getY()), shooter.getProjectiles(), blocks);
    }

    @Override
    public boolean hasCollision() {
        return false;
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
