package org.example;

import java.awt.*;
import java.awt.geom.Area;
import java.util.ArrayList;

import static java.lang.Math.round;
import static java.util.Arrays.stream;
import static java.lang.Math.*;

public class PolyUtils {
    public static boolean intersects(Polygon first, Polygon second) {
        if (!first.getBounds().intersects(second.getBounds()))
            return false;
        for (int i = 0; i < first.npoints; i++)
            if (second.contains(first.xpoints[i], first.ypoints[i]))
                return true;
        for (int i = 0; i < second.npoints; i++)
            if (first.contains(second.xpoints[i], second.ypoints[i]))
                return true;

        return true;
    }

    public static int getHeight(Polygon poly) {
        return stream(poly.ypoints).max().orElseThrow() - stream(poly.ypoints).min().orElseThrow();
    }

    public static int getWidth(Polygon poly) {
        return stream(poly.xpoints).max().orElseThrow() - stream(poly.xpoints).min().orElseThrow();
    }

    public static Polygon from(Rectangle rect) {
        // Create a Polygon and add the four corners of the rectangle
        Polygon polygon = new Polygon();

        polygon.addPoint(rect.x, rect.y); // Top-left corner
        polygon.addPoint(rect.x + rect.width, rect.y); // Top-right corner
        polygon.addPoint(rect.x + rect.width, rect.y + rect.height); // Bottom-right corner
        polygon.addPoint(rect.x, rect.y + rect.height); // Bottom-left corner

        return polygon;
    }

    public static Polygon from(Area area) {
        var poly = new Polygon();
        area.getPathIterator(null).currentSegment(new double[6]);
        var pathIterator = area.getPathIterator(null);
        double[] coords = new double[6];

        while (!pathIterator.isDone()) {
            pathIterator.currentSegment(coords);
            poly.addPoint((int) coords[0], (int) coords[1]);
            pathIterator.next();
        }

        return poly;
    }

    public static Polygon from(Rectangle rect, double theta) {
        return rotate(from(rect), theta);
    }

    public static Polygon rotate(Polygon poly, double theta) {
        var center = getCenter(poly);

        poly = new Polygon(poly.xpoints, poly.ypoints, poly.npoints);
        for (int i = 0; i < poly.npoints; i++) {
            int x = poly.xpoints[i], y = poly.ypoints[i];
            poly.xpoints[i] = (int) round((x - center.x) * cos(theta) - (y - center.y) * sin(theta) + center.x);
            poly.ypoints[i] = (int) round((x - center.x) * sin(theta) + (y - center.y) * cos(theta) + center.y);
        }
        return poly;
    }

    public static Point roundedPoint(double x, double y) {
        return new Point((int) round(x), (int) round(y));
    }

    public static Point getCenter(Polygon poly) {
        var bounds = poly.getBounds();
        return roundedPoint(bounds.getCenterX(), bounds.getCenterY());
    }

    public static Point getCenter(Rectangle rectangle) {
        return roundedPoint(rectangle.getCenterX(), rectangle.getCenterY());
    }

    public static Point[] getCorners(Polygon poly) {
        var corners = new ArrayList<Point>();
        for (int i = 0; i < poly.npoints; ++i) {
            corners.add(new Point(poly.xpoints[i], poly.ypoints[i]));
        }
        return corners.toArray(new Point[0]);
    }

    public static Polygon intersection(Polygon first, Polygon second) {
        var firstArea = new Area(first);
        var secondArea = new Area(second);
        firstArea.intersect(secondArea);
        return from(firstArea);
    }
}
