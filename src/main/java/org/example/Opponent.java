package org.example;

import java.awt.*;
import java.util.Map;

import static org.example.ConnectionHandler.*;

public class Opponent extends Player implements ConnectionHandler.MessageObserver {
    public Opponent(GamePanel gamePanel, String name) {
        super(gamePanel, name);
    }

    @Override
    public void update(double dt) {
    }

    @Override
    public void handleMessage(Map<String, Object> message) {
        if (message.get("request_type").equals("game-state")) {
            var data = message.get("data");
            var updateInfo = GSON.fromJson(GSON.toJson(data), PlayerUpdateInfo.class);
            setX(updateInfo.x());
            setY(updateInfo.y());
            setBlocks(updateInfo.blocks());
            setProjectiles(updateInfo.projectiles());
            setHealth(updateInfo.health());
            setShooterAngle(updateInfo.shooterAngle());
            /*
            {
                "blocks": [
                    {
                        "position": {
                            "x": x,
                            "y": y
                        }
                        "health": health
                    }
                ],
                "projectiles": [
                    {
                        "x": x,
                        "y": y,
                        "angle": angle
                    }
                ],
                "health": health,
                "shooterAngle": shooterAngle
            }
            */
        }
    }

    @Override
    public boolean tracked() {
        return false;
    }

    @Override
    public Color getColor() {
        return Color.RED;
    }
}

