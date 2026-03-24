package org.example;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
            if (data.containsKey("x")) {
                setX(numberValue(data.get("x")));
            }
            if (data.containsKey("y")) {
                setY(numberValue(data.get("y")));
            }
            if (data.containsKey("blocks")) {
                setBlocks(parseBlocks(data.get("blocks")));
            }
            if (data.containsKey("projectiles")) {
                setProjectiles(parseProjectiles(data.get("projectiles")));
            }
            if (data.containsKey("health")) {
                setHealth(numberValue(data.get("health")));
            }
            if (data.containsKey("shooterAngle")) {
                setShooterAngle(numberValue(data.get("shooterAngle")));
            }
            if (data.containsKey("facingLeft")) {
                setFacingLeft(booleanValue(data.get("facingLeft")));
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

    public void destroy() {
        setBlocks(new ArrayList<Block>());
        setProjectiles(new ArrayList<Projectile>());
        destroy = true;
    }

    @Override
    public boolean observing() {
        return !destroy;
    }

    private List<Block> parseBlocks(Object rawBlocks) {
        ArrayList<Block> parsedBlocks = new ArrayList<Block>();
        if (!(rawBlocks instanceof List<?>)) {
            return parsedBlocks;
        }
        for (Object rawBlock : (List<?>) rawBlocks) {
            if (!(rawBlock instanceof Map<?, ?>)) {
                continue;
            }
            Map<?, ?> blockMap = (Map<?, ?>) rawBlock;
            Block block = new Block();
            block.restoreState(
                    parseVec2(blockMap.get("position")),
                    numberValue(blockMap.get("health")),
                    stringValue(blockMap.get("id"))
            );
            parsedBlocks.add(block);
        }
        return parsedBlocks;
    }

    private List<Projectile> parseProjectiles(Object rawProjectiles) {
        ArrayList<Projectile> parsedProjectiles = new ArrayList<Projectile>();
        if (!(rawProjectiles instanceof List<?>)) {
            return parsedProjectiles;
        }
        for (Object rawProjectile : (List<?>) rawProjectiles) {
            if (!(rawProjectile instanceof Map<?, ?>)) {
                continue;
            }
            Map<?, ?> projectileMap = (Map<?, ?>) rawProjectile;
            Projectile projectile = new Projectile();
            projectile.restoreState(
                    parseVec2(projectileMap.get("position")),
                    parseVec2(projectileMap.get("velocity")),
                    numberValue(projectileMap.get("angle")),
                    stringValue(projectileMap.get("id"))
            );
            parsedProjectiles.add(projectile);
        }
        return parsedProjectiles;
    }

    private Vec2 parseVec2(Object rawVec2) {
        if (!(rawVec2 instanceof Map<?, ?>)) {
            return Vec2.zero();
        }
        Map<?, ?> vecMap = (Map<?, ?>) rawVec2;
        return Vec2.of(
                numberValue(vecMap.get("x")),
                numberValue(vecMap.get("y"))
        );
    }

    private double numberValue(Object value) {
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        return 0;
    }

    private boolean booleanValue(Object value) {
        if (value instanceof Boolean) {
            return ((Boolean) value).booleanValue();
        }
        return false;
    }

    private String stringValue(Object value) {
        if (value == null) {
            return null;
        }
        return String.valueOf(value);
    }
}
