package org.example;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static java.lang.Math.abs;
import static java.lang.System.nanoTime;
import static java.util.Arrays.stream;
import static org.example.PolyUtils.intersects;
import static org.example.PolyUtils.rotate;
import static org.example.Vec2.zero;
import static org.example.Wall.DOWN;
import static org.example.Wall.LEFT;
import static org.example.Wall.RIGHT;
import static org.example.Wall.UP;

public class PhysicsHandler {
    public static final double GRAVITATIONAL_ACCELERATION = 0.005;
    private final List<GravityApplier> appliers;
    private final GamePanel gamePanel;

    public PhysicsHandler(GamePanel gamePanel, PhysicsObject... objects) {
        this.gamePanel = gamePanel;
        this.appliers = Collections.synchronizedList(stream(objects)
                .map(GravityApplier::new)
                .collect(Collectors.toCollection(ArrayList<GravityApplier>::new)));
        appliers.forEach(applier -> applier.getObject().setGravityApplier(applier));
    }

    public void update(double dt) {
        synchronized (appliers) {
            appliers.removeIf(applier -> !applier.getObject().tracked());
            appliers.forEach(applier -> {
                if (applier != null) {
                    applier.apply(dt);
                    applier.prevVelocity.set(applier.gravityVelocity);
                }
            });
            handleObjectCollisions();
            handleWallCollisions();
            applyGravity(dt);
            handleObjectCollisions();
            handleWallCollisions();
        }
    }

    private void applyGravity(double dt) {
        appliers.forEach(applier -> {
            Vec2 velocity = applier.getGravityVelocity();
            velocity.set(velocity.add(Vec2.of(0, GRAVITATIONAL_ACCELERATION * applier.getObject().getMass() * dt)));
        });
    }

    private void handleWallCollisions() {
        Rectangle bounds = new Rectangle(gamePanel.getSize());

        appliers.forEach(applier -> {
            PhysicsObject object = applier.getObject();
            Polygon box = object.getCollisionPoly();
            Rectangle bBounds = box.getBounds();
            Polygon original = rotate(box, -object.getAngle());
            double distX = abs(stream(box.xpoints).min().getAsInt() - stream(original.xpoints).min().getAsInt());
            double distY = abs(stream(box.ypoints).min().getAsInt() - stream(original.ypoints).min().getAsInt());
            if (!bounds.contains(bBounds)) {
                ArrayList<Wall> walls = new ArrayList<Wall>();
                int leftX = stream(box.xpoints).min().getAsInt();
                int upY = stream(box.ypoints).min().getAsInt();
                int rightX = stream(box.xpoints).max().getAsInt();
                int downY = stream(box.ypoints).max().getAsInt();
                if (upY <= bounds.y) {
                    object.setY(distY + 1);
                    if (!(object instanceof Player)) {
                        walls.add(UP);
                    }
                }

                if (leftX <= bounds.y) {
                    object.setX(distX + 1);
                    walls.add(LEFT);
                }

                if (downY >= bounds.height) {
                    applier.getGravityVelocity().setY(0);
                    object.setY(bounds.y + bounds.height - downY + upY - distY - 1);
                    walls.add(DOWN);
                }

                if (rightX >= bounds.width) {
                    object.setX(bounds.x + bounds.width - rightX + leftX - distX * 2 - 1);
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
            PhysicsObject first = appliers.get(i).getObject();
            for (int j = i + 1; j < appliers.size(); j++) {
                PhysicsObject second = appliers.get(j).getObject();
                if (first.hasCollision() && second.hasCollision() && intersects(first.getCollisionPoly(), second.getCollisionPoly())) {
                    boolean skip = second instanceof Projectile && first instanceof Projectile;
                    if (!skip && first instanceof Player && second instanceof Projectile) {
                        skip = ((Projectile) second).getPlayer() == first;
                    }
                    if (!skip && second instanceof Player && first instanceof Projectile) {
                        skip = ((Projectile) first).getPlayer() == second;
                    }
                    if (!skip) {
                        first.handleObjectCollision(second);
                        second.handleObjectCollision(first);
                        long now = nanoTime();
                        first.setLastCollision(now);
                        second.setLastCollision(now);
                        restrict(first, second);
                    }
                }
            }
        }
    }

    public static void restrict(PhysicsObject first, PhysicsObject second) {
        if ((first instanceof Player && second instanceof Projectile) || (first instanceof Projectile && second instanceof Player)) {
            return;
        }
        Rectangle firstBounds = first.getCollisionPoly().getBounds();
        Rectangle secondBounds = second.getCollisionPoly().getBounds();

        double overlapX = Math.min(firstBounds.getMaxX() - secondBounds.getMinX(), secondBounds.getMaxX() - firstBounds.getMinX());
        double overlapY = Math.min(firstBounds.getMaxY() - secondBounds.getMinY(), secondBounds.getMaxY() - firstBounds.getMinY());

        if (overlapX < overlapY) {
            double separation = overlapX * (firstBounds.getCenterX() > secondBounds.getCenterX() ? 1 : -1);
            first.getPosition().setX(first.getPosition().getX() + separation / 2);
            second.getPosition().setX(second.getPosition().getX() - separation / 2);
        } else {
            double separation = overlapY * (firstBounds.getCenterY() > secondBounds.getCenterY() ? 1 : -1);
            if (firstBounds.getCenterY() < secondBounds.getCenterY()) {
                if (first.getGravityApplier() != null) {
                    first.getGravityApplier().getGravityVelocity().set(zero());
                }
            } else {
                if (second.getGravityApplier() != null) {
                    second.getGravityApplier().getGravityVelocity().set(zero());
                }
            }
            first.getPosition().setY(first.getPosition().getY() + separation / 2);
            second.getPosition().setY(second.getPosition().getY() - separation / 2);
        }
    }

    public List<PhysicsObject> getObjects() {
        return appliers.stream().map(GravityApplier::getObject).collect(Collectors.toList());
    }

    public List<GravityApplier> getAppliers() {
        return appliers;
    }

    public void reset() {
        this.appliers.clear();
    }

    public void trackObject(PhysicsObject object) {
        GravityApplier applier = new GravityApplier(object);
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
            Vec2 position = object.getPosition();
            Vec2 velocity = object.getVelocity().mul(dt);
            velocity.set(velocity.add(Vec2.of(0, this.gravityVelocity.getY() * dt)));
            object.setPosition(velocity.add(position));
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
