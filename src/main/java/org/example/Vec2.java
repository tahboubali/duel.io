package org.example;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.function.BinaryOperator;

import static java.lang.Math.*;

public class Vec2 {
    private double x, y;
    public final static BinaryOperator<Double>
            ADD = Double::sum,
            SUBTRACT = (a, b) -> a - b,
            MULTIPLY = (a, b) -> a * b,
            DIVIDE = (a, b) -> a / b;

    private Vec2(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public Vec2 op(Vec2 other, BinaryOperator<Double> op) {
        return new Vec2(
                op.apply(this.x, other.x),
                op.apply(this.y, other.y)
        );
    }

    public Vec2 op(double by, BinaryOperator<Double> op) {
        return new Vec2(
                op.apply(this.x, by),
                op.apply(this.y, by)
        );
    }

    public Vec2 mul(Vec2 other) {
        return op(other, MULTIPLY);
    }

    public Vec2 div(Vec2 other) {
        return op(other, DIVIDE);
    }

    public Vec2 add(Vec2 other) {
        return op(other, ADD);
    }

    public Vec2 sub(Vec2 other) {
        return op(other, SUBTRACT);
    }

    public Vec2 mul(double scale) {
        return op(scale, MULTIPLY);
    }

    public Vec2 div(double scale) {
        return op(scale, DIVIDE);
    }

    public Vec2 add(double plus) {
        return op(plus, ADD);
    }

    public Vec2 sub(double minus) {
        return op(minus, SUBTRACT);
    }

    public double dot(Vec2 other) {
        return x * other.x + y * other.y;
    }

    public void setX(double x) {
        this.x = x;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getY() {
        return y;
    }

    public void set(Vec2 other) {
        this.x = other.x;
        this.y = other.y;
    }

    public void set(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double getX() {
        return x;
    }

    public Point asPoint() {
        return new Point((int) round(x), (int) round(y));
    }

    public static Vec2 of(Point point) {
        return new Vec2(point.x, point.y);
    }

    public static Vec2 of(double x, double y) {
        return new Vec2(x, y);
    }

    public static Vec2 zero() {
        return new Vec2(0, 0);
    }

    public double angleTo(Vec2 other) {
        var sub = sub(other);
        return Math.atan2(sub.y, sub.x);
    }

    public double angleTo(Point point) {
        return angleTo(of(point));
    }

    public double asAngle() {
        return Math.atan2(y, x);
    }

    public Vec2 normalized() {
        double mag = hypot(x, y);
        if (mag == 0)
            return zero();
        return this.div(mag);
    }

    public void normalize() {
        this.set(normalized());
    }

    public static Vec2 up() {
        return new Vec2(0, -1);
    }

    public static Vec2 left() {
        return new Vec2(-1, 0);
    }

    public static Vec2 down() {
        return new Vec2(0, 1);
    }

    public static Vec2 right() {
        return new Vec2(1, 0);
    }

    public static Vec2 delta(Point p1, Point p2) {
        return new Vec2(p2.getX() - p1.getX(), p2.getY() - p1.getY());
    }

    public static Vec2 delta(Point2D p1, Point2D p2) {
        return new Vec2(p2.getX() - p1.getX(), p2.getY() - p1.getY());
    }

    public static Vec2 abs(Vec2 vec2) {
        return new Vec2(Math.abs(vec2.x), Math.abs(vec2.y));
    }

    public double magnitude() {
        return hypot(x, y);
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Vec2 vec2)) return false;

        return Double.compare(x, vec2.x) == 0 && Double.compare(y, vec2.y) == 0;
    }

    @Override
    public int hashCode() {
        int result = Double.hashCode(x);
        result = 31 * result + Double.hashCode(y);
        return result;
    }

    @Override
    public String toString() {
        return "(%f, %f)".formatted(x, y);
    }
}
