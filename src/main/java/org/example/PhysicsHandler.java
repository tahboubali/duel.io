package org.example;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Arrays.stream;
import static org.example.Vec2.zero;
import static org.example.Wall.*;

public class PhysicsHandler {
    public static final double GRAVITATIONAL_ACCELERATION = 0.005;
    private final List<GravityApplier> appliers;
    private final GamePanel gamePanel;

    public PhysicsHandler(GamePanel gamePanel, PhysicsObject... objects) {
        this.gamePanel = gamePanel;
        this.appliers = stream(objects).map(GravityApplier::new).collect(Collectors.toCollection(ArrayList::new));
        appliers.forEach(applier -> applier.getObject().setGravityApplier(applier));
    }

    public void update(double dt) {
        appliers.removeIf(applier -> !applier.getObject().tracked());
        appliers.forEach(applier -> {
            applier.apply(dt);
            applier.prevVelocity.set(applier.gravityVelocity);
        });
        applyGravity(dt);
        handleWallCollisions();
        handleObjectCollisions();
    }

    private void applyGravity(double dt) {
        appliers.forEach(applier -> {
            var velocity = applier.getGravityVelocity();
            velocity.set(velocity.add(Vec2.of(0, GRAVITATIONAL_ACCELERATION * applier.getObject().getMass() * dt)));
        });
    }

    private void handleWallCollisions() {
        var bounds = gamePanel.getBounds();
        appliers.forEach(applier -> {
            var object = applier.getObject();
            var box = object.getCollision();
            if (!bounds.contains(box)) {
                var walls = new ArrayList<Wall>();
                if (box.y < bounds.y) {
                    object.setY(bounds.y);
                    walls.add(UP);
                }

                if (box.x < bounds.x) {
                    object.setX(bounds.x);
                    walls.add(LEFT);
                }

                if (box.y + box.height > bounds.y + bounds.height) {
                    object.setY(bounds.y + bounds.height - box.height);
                    walls.add(DOWN);
                    applier.getGravityVelocity().setY(0);
                }

                if (box.x + box.width > bounds.x + bounds.width) {
                    object.setX(bounds.x + bounds.width - box.width);
                    walls.add(RIGHT);
                }

                walls.forEach(wall -> applier.gravityVelocity.set(
                        applier.gravityVelocity.sub(
                                wall.normal().mul(2 * applier.gravityVelocity.dot(wall.normal()))
                        )
                ));

                object.handleWallCollision(walls.toArray(new Wall[0]));
            }
        });
    }

    private void handleObjectCollisions() {
        for (int i = 0; i < appliers.size(); i++) {
            var first = appliers.get(i).getObject();
            for (int j = i + 1; j < appliers.size(); j++) {
                var second = appliers.get(j).getObject();
                if (first.hasCollision() && second.hasCollision() && first.getCollision().intersects(second.getCollision())) {
                    first.handleObjectCollision(second);
                    second.handleObjectCollision(first);
                }
            }
        }
    }

    public void trackObject(PhysicsObject object) {
        var applier = new GravityApplier(object);
        appliers.add(applier);
        object.setGravityApplier(applier);

    }

    public static class GravityApplier {
        private final PhysicsObject object;
        private final Vec2 gravityVelocity = zero();
        private final Vec2 prevVelocity = zero();

        public GravityApplier(PhysicsObject object) {
            this.object = object;
        }

        public void apply(double dt) {
            var position = object.getPosition();
            var velocity = object.getVelocity().mul(dt)
                    .add(Vec2.of(0, this.gravityVelocity.getY() * dt));
            object.setPosition((velocity.add(position)));
            object.setAngle(velocity.asAngle());
        }

        public Vec2 getGravityVelocity() {
            return gravityVelocity;
        }

        public Vec2 getPrevVelocity() {
            return prevVelocity;
        }

        public PhysicsObject getObject() {
            return object;
        }
    }
}