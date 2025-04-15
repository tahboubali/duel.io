package org.example;

import java.awt.*;
import java.util.Map;

import static org.example.ConnectionHandler.*;

public class Opponent extends Player implements ConnectionHandler.MessageObserver {
    private boolean destroy;

    public Opponent(GamePanel gamePanel, String name) {
        super(gamePanel, name);
        gamePanel.addPhysicsObject(this);
    }

    @Override
    public void update(double dt) {
    }

    @Override
    public void handleMessage(Map<String, Object> message) {
        if (message.get("request_type").equals("game-state")) {
            var data = (Map<?, ?>) message.get("data");
            var updateInfo = GSON.fromJson(GSON.toJson(data), PlayerUpdateInfo.class);
            if (data.containsKey("x")) {
                setX(updateInfo.x());
            }
            if (data.containsKey("y")) {
                setY(updateInfo.y());
            }
            if (data.containsKey("blocks")) {
                setBlocks(updateInfo.blocks());
                for (var block : getBlocks()) {
                    block.setGravityApplier(null);
                }
            }
            if (data.containsKey("projectiles")) {
                setProjectiles(updateInfo.projectiles());
                for (var projectile : getProjectiles()) {
                    projectile.setGravityApplier(null);
                }
            }
            if (data.containsKey("health")) {
                setHealth(updateInfo.health());
            }
            if (data.containsKey("shooterAngle")) {
                setShooterAngle(updateInfo.shooterAngle());
            }
            if (data.containsKey("facingLeft")) {
                setFacingLeft(updateInfo.facingLeft());
            }
        }
    }

    @Override
    public boolean tracked() {
        return !destroy;
    }

    @Override
    public void handleObjectCollision(PhysicsObject obj) {
        super.handleObjectCollision(obj);
    }

    @Override
    public Color getColor() {
        return Color.RED;
    }

    @Override
    public PhysicsHandler.GravityApplier getGravityApplier() {
        return null;
    }

    public void destroy() {
        destroy = true;
    }
}

