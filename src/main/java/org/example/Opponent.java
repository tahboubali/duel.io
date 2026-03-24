package org.example;

import java.awt.*;
import java.util.ArrayList;
import java.util.Map;

import static org.example.ConnectionHandler.GSON;

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
        Object requestType = message.get("request_type");
        Map<?, ?> data = (Map<?, ?>) message.get("data");
        if ("game-state".equals(requestType)) {
            ConnectionHandler.PlayerUpdateInfo updateInfo = GSON.fromJson(GSON.toJson(data), ConnectionHandler.PlayerUpdateInfo.class);
            if (data.containsKey("x")) {
                setX(updateInfo.x());
            }
            if (data.containsKey("y")) {
                setY(updateInfo.y());
            }
            if (data.containsKey("blocks")) {
                setBlocks(updateInfo.blocks());
            }
            if (data.containsKey("projectiles")) {
                setProjectiles(updateInfo.projectiles());
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
        } else if ("health-update".equals(requestType)) {
            double delta = ((Number) data.get("delta")).doubleValue();
            Main.getGamePanel().changePlayerHealth(delta);
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
        setBlocks(new ArrayList<Block>());
        setProjectiles(new ArrayList<Projectile>());
        destroy = true;
    }

    @Override
    public boolean observing() {
        return !destroy;
    }
}
