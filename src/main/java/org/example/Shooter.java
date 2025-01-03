package org.example;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.lang.Math.*;
import static org.example.PolyUtils.getHeight;
import static org.example.PolyUtils.getWidth;

public class Shooter implements GameObj {
    private final List<Projectile> projectiles;
    private final Player player;
    public int xOffset, yOffset;
    private Vec2 direction;
    private final GamePanel gamePanel;
    private boolean facingLeft;

    public Shooter(Player player, GamePanel gamePanel) {
        this.player = player;
        this.projectiles = Collections.synchronizedList(new ArrayList<>());
        this.direction = Vec2.zero();
        this.gamePanel = gamePanel;
        this.yOffset = -10;
    }

    public void shoot() {
        var bullet = new Projectile(gamePanel, getX(), getY(), direction);
        projectiles.add(bullet);
        gamePanel.addPhysicsObject(bullet);
    }

    @Override
    public void update(double dt) {
        updateDirection();
        projectiles.stream().filter(Projectile::shouldDespawn).forEach(_ -> System.out.println("SHOULD DESPAWN"));
        projectiles.removeIf(Projectile::shouldDespawn);
        projectiles.forEach(projectile -> projectile.update(dt));
    }

    private void updateDirection() {
        var mousePosition = gamePanel.getMousePosition();
        if (mousePosition == null) return;
        var playerCenterPos = player.getPosition().add(Vec2.of(getWidth(player.getCollisionPoly()) / 2d, getHeight(player.getCollisionPoly())  / 2d));
        facingLeft = abs(playerCenterPos.angleTo(mousePosition)) < PI / 2;
        var gunPos = Vec2.of(getX(), getY());
        var delta = Vec2.of(mousePosition).sub(gunPos);
        double distance = gunPos.asPoint().distance(mousePosition);
        if (distance != 0) direction = delta.div(distance);
    }

    @Override
    public void draw(Graphics2D g2d) {
        var transform = g2d.getTransform();
        drawGun(g2d);
        g2d.setTransform(transform);
        projectiles.forEach(projectile -> projectile.draw(g2d));
    }

    public void drawGun(Graphics2D g2d) {
        var r1 = new Rectangle(getX(), getY(), 28, 14);
        var r2 = new Rectangle(getX(), getY(), 11, 20);
        if (facingLeft) {
            r1.y += r2.height - r1.height;
            xOffset = -getWidth(player.getCollisionPoly()) / 2;
        } else {
            xOffset = getWidth(player.getCollisionPoly()) / 2;
        }
        xOffset -= r1.width / 2;
        g2d.drawString(String.valueOf(toDegrees(getAngle())), 10, 100);
        g2d.setColor(Color.BLACK);
        g2d.rotate(getAngle(), r1.x + r1.getWidth() / 2, r2.y + r2.getHeight() / 2);
        g2d.fill(r1);
        g2d.fill(r2);
    }

    private int getX() {
        return (int) round(player.getX() + getWidth(player.getCollisionPoly()) / 2d) + xOffset;
    }

    private int getY() {
        return (int) round(player.getY() + getHeight(player.getCollisionPoly())  / 2d) + yOffset;
    }

    private double getAngle() {
        return direction.asAngle();
    }

    public List<Projectile> getProjectiles() {
        return Collections.unmodifiableList(projectiles);
    }
}
