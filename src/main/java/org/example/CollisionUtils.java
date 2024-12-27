package org.example;

import java.awt.*;

import static java.util.Arrays.stream;

public class CollisionUtils {
    public static boolean intersects(Polygon first, Polygon second) {
        if (!first.getBounds().intersects(second.getBounds()))
            return false;
        for (int i = 0; i < first.npoints; i++)
            if (second.contains(first.xpoints[i], first.ypoints[i]))
                return true;
        for (int i = 0; i < second.npoints; i++)
            if (first.contains(second.xpoints[i], second.ypoints[i]))
                return true;

        return false;
    }

    public static int getHeight(Polygon polygon) {
        return stream(polygon.ypoints).max().orElseThrow() - stream(polygon.ypoints).min().orElseThrow();
    }

    public static int getWidth(Polygon polygon) {
        return stream(polygon.xpoints).max().orElseThrow() - stream(polygon.xpoints).min().orElseThrow();
    }

    public static Polygon fromRect(Rectangle rect) {

    }
}
