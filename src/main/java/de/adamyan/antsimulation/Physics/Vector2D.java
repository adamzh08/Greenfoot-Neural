package de.adamyan.antsimulation.Physics;

public class Vector2D {
    private double x;
    private double y;

    public Vector2D(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double magnitudeSquared() {
        return x * x + y * y;
    }

    public double magnitude() {
        return Math.sqrt(magnitudeSquared());
    }
    public double slope() {
        return y / x;
    }
    public double angle() {
        return Math.atan2(y, x);
    }

    public void add(Vector2D other) {
        x += other.x;
        y += other.y;
    }

    public void scale(double scalar) {
        x *= scalar;
        y *= scalar;
    }

    public void normalize() {
        scale(1 / magnitude());
    }

    public double x() {
        return x;
    }

    public double y() {
        return y;
    }

    public void setX(double newX) {
        this.x = newX;
    }
    public void setY(double newY) {
        this.y = newY;
    }



    public static Vector2D ZERO() {
        return new Vector2D(0, 0);
    }

    @Override
    public Vector2D clone() {
        return new Vector2D(x, y);
    }
}
