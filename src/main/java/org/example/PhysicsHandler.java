package org.example;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static java.lang.Math.*;
import static java.lang.System.nanoTime;
import static java.util.Arrays.stream;
import static org.example.PolyUtils.*;
import static org.example.Vec2.zero;
import static org.example.Wall.*;

public class PhysicsHandler {
    public static final double GRAVITATIONAL_ACCELERATION = 0.005;
    private final List<GravityApplier> appliers;
    private final GamePanel gamePanel;
    private final static long COLLISION_COOLDOWN_NANOS = Duration.ofMillis(15).toNanos();

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
            var box = object.getCollisionPoly();
            var bBounds = box.getBounds();
            var original = rotate(box, -object.getAngle());
            double distX = abs(stream(box.xpoints).min().orElseThrow() - stream(original.xpoints).min().orElseThrow());
            double distY = abs(stream(box.ypoints).min().orElseThrow() - stream(original.ypoints).min().orElseThrow());
            if (!bounds.contains(bBounds) && collisionCooledDown(object)) {
                var walls = new ArrayList<Wall>();
                int leftX = stream(box.xpoints).min().orElseThrow(),
                        upY = stream(box.ypoints).min().orElseThrow(),
                        rightX = stream(box.xpoints).max().orElseThrow(),
                        downY = stream(box.ypoints).max().orElseThrow();
                if (upY <= 0) {
                    object.setY(distY);
                    if (!(object instanceof Player))
                        walls.add(UP);
                }

                if (leftX <= 0) {
                    object.setX(distX);
                    walls.add(LEFT);
                }

                if (downY >= bounds.height) {
                    applier.getGravityVelocity().setY(0);
                    object.setY(bounds.y + bounds.height - downY + upY - distY);
                    walls.add(DOWN);
                }

                if (rightX >= bounds.width) {
                    object.setX(bounds.x + bounds.width - rightX + leftX - distX * 2);
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
                if ((collisionCooledDown(first) || collisionCooledDown(second))
                        && first.hasCollision() && second.hasCollision()
                        && intersects(first.getCollisionPoly(), second.getCollisionPoly())) {
                    boolean skip = second instanceof Projectile && first instanceof Projectile;
                    skip = skip || (first instanceof Player player && second instanceof Projectile projectile && projectile.getPlayer() == player);
                    skip = skip || (second instanceof Player player && first instanceof Projectile projectile && projectile.getPlayer() == player);

                    if (!skip) {
                        first.handleObjectCollision(second);
                        second.handleObjectCollision(first);
                        long now = nanoTime();
                        first.setLastCollision(now);
                        second.setLastCollision(now);
                        if (!((first instanceof Projectile && second instanceof Block) || (first instanceof Block && second instanceof Projectile)))
                            restrict(first, second);
                    }
                }
            }
        }
    }

    public static void restrict(PhysicsObject first, PhysicsObject second) {
        var firstBounds = first.getCollisionPoly().getBounds();
        var secondBounds = second.getCollisionPoly().getBounds();

        double overlapX = Math.min(
                firstBounds.getMaxX() - secondBounds.getMinX(),
                secondBounds.getMaxX() - firstBounds.getMinX()
        );

        double overlapY = Math.min(
                firstBounds.getMaxY() - secondBounds.getMinY(),
                secondBounds.getMaxY() - firstBounds.getMinY()
        );

        if (overlapX < overlapY) {
            double separation = overlapX * (firstBounds.getCenterX() > secondBounds.getCenterX() ? 1 : -1);
            first.getPosition().setX(first.getPosition().getX() + separation / 2);
            second.getPosition().setX(second.getPosition().getX() - separation / 2);
        } else {
            double separation = overlapY * (firstBounds.getCenterY() > secondBounds.getCenterY() ? 1 : -1);
            if (firstBounds.getCenterY() < secondBounds.getCenterY()) {
                first.getGravityApplier().getGravityVelocity().set(zero());
            } else {
                second.getGravityApplier().getGravityVelocity().set(zero());
            }
            first.getPosition().setY(first.getPosition().getY() + separation / 2);
            second.getPosition().setY(second.getPosition().getY() - separation / 2);
        }
    }

    public List<PhysicsObject> getObjects() {
        return appliers.stream().map(GravityApplier::getObject).toList();
    }

    private static boolean collisionCooledDown(PhysicsObject object) {
        return nanoTime() - object.lastCollision() > COLLISION_COOLDOWN_NANOS;
    }

    public void trackObject(PhysicsObject object) {
        var applier = new GravityApplier(object);
        appliers.add(applier);
        object.setGravityApplier(applier);
    }

    public void restrict(PhysicsObject obj) {
        for (var other : appliers) {
            var otherObj = other.getObject();
            if (otherObj == obj) continue;
            restrict(obj, otherObj);
        }
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