package org.example;

import java.awt.*;

public class CollisionPoly {
    private Polygon polygon;

    private CollisionPoly(Polygon polygon) {
        this.polygon = polygon;
    }

    public static CollisionPoly of(Polygon poly) {
        return new CollisionPoly(poly);
    }

    public boolean collidesWith(CollisionPoly other) {
        if (!polygon.getBounds().intersects(other.polygon.getBounds()))
            return false;
        for (int i = 0; i < polygon.npoints; i++)
            if (other.polygon.contains(polygon.xpoints[i], polygon.ypoints[i]))
                return true;
        for (int i = 0; i < other.polygon.npoints; i++)
            if (polygon.contains(other.polygon.xpoints[i], other.polygon.ypoints[i]))
                return true;

        return false;
    }
}
