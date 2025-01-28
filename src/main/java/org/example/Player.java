package org.example;

import java.awt.*;

import static java.lang.Math.*;
import static org.example.KeyHandler.*;
import static org.example.MouseHandler.isLeftClicked;
import static org.example.MouseHandler.isRightClicked;
import static org.example.ConnectionHandler.*;
import static org.example.PolyUtils.*;

import java.util.ArrayList;
import java.util.List;

public class Player implements PhysicsObject {
    private static double MAX_HEALTH = 8;
    public static final int WIDTH = 30;
    public static final int HEIGHT = 66;
    private static final int SHOT_COOLDOWN_MILLIS = 320,
            BUILD_COOLDOWN_MILLIS = 350; // 350
    private final static double SPEED = .5;
    private final static double JUMP_VELOCITY = 1.4;
    private final Shooter shooter;
    private final Vec2 position;
    private long lastShot, lastBuilt;
    private final List<Block> blocks;
    private boolean jumping;
    private final GamePanel gamePanel;
    private Vec2 velocity;
    private final String name;
    private PhysicsHandler.GravityApplier gravityApplier;
    private long lastCollision;
    private double health;

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
            var direction = shooter.getDirection().mul(100);
            var block = new Block(position.getX() + WIDTH / 2d + direction.getX() - Block.WIDTH / 2d,
                    position.getY() + HEIGHT / 2d + direction.getY() - Block.HEIGHT / 2d, Color.GREEN, this, gamePanel);
            block.getVelocity().set(direction.mul(.002));
            var intersects = false;
            for (var other : blocks.stream().map(Block::getCollisionPoly).toList()) {
                if (intersects(block.getCollisionPoly(), other)) {
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
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(2.3f));
        g2d.drawRect(point.x, point.y, WIDTH, HEIGHT);
        drawUsername(g2d);
        drawHealthBar(g2d);
        synchronized (blocks) {
            shooter.draw(g2d);
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
        setPosition(Vec2.of(x, getY()));
    }

    @Override
    public void setY(double y) {
        setPosition(Vec2.of(getX(), y));
    }

    @Override
    public void setPosition(Vec2 vec2) {
        for (var object : gamePanel.getPhysicsObjects()) {
            if (object == this) continue;
            if (this.getCollisionPoly().getBounds().intersects(object.getCollisionPoly().getBounds()))
                PhysicsHandler.restrict(this, object);
        }
        position.set(Vec2.of(
                max(0, min(vec2.getX(), gamePanel.getWidth() - WIDTH + .5)),
                max(0, min(vec2.getY(), gamePanel.getHeight() - HEIGHT + .5))
        ));

    }

    @Override
    public Vec2 getPosition() {
        return position;
    }

    @Override
    public Polygon getCollisionPoly() {
        return from(new Rectangle((int) round(position.getX()), (int) round(position.getY()), WIDTH, HEIGHT));
    }

    @Override
    public double getMass() {
        return 1;
    }

    @Override
    public void handleObjectCollision(PhysicsObject obj) {
        if (obj instanceof Block) {
            if (position.getY() + HEIGHT >= obj.getY() && getY() < obj.getY()) {
                jumping = false;
                getGravityApplier().getGravityVelocity().setY(0);
            }
        }
        if (obj instanceof Projectile projectile) {
            health -= projectile.getVelocity().magnitude();
        }
    }

    @Override
    public double getAngle() {
        return 0;
    }

    @Override
    public void handleWallCollision(Wall... walls) {

    }

    @Override
    public void setAngle(double angle) {
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
        return true;
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

    private void drawHealthBar(Graphics2D g2d) {
        g2d.setStroke(new BasicStroke(1.3f));
        int healthBarWidth = WIDTH + 5;
        int healthBarHeight = 10;
        var asPoint = position.asPoint();
        int healthBarY = asPoint.y - 22 - healthBarHeight, healthBarX = (asPoint.x + WIDTH / 2) - healthBarWidth / 2;
        g2d.drawRect(healthBarX, healthBarY, healthBarWidth, healthBarHeight);
    }

    private void drawUsername(Graphics2D g2d) {
        var fontMetrics = g2d.getFontMetrics();
        int usernameWidth = fontMetrics.stringWidth(name);
        int offset = fontMetrics.getHeight() + 15;
        var asPoint = position.asPoint();
        int nameX = asPoint.x - usernameWidth / 2 + WIDTH / 2;
        int nameY = (asPoint.y - offset) - fontMetrics.getHeight() / 2 + HEIGHT / 2;
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(5f));
        g2d.drawString(name, nameX, nameY);
    }
}
