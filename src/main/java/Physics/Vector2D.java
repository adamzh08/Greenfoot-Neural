package Physics;

public record Vector2D(double x, double y) {
    public Vector2D subtractFrom(Vector2D other) {
        return new Vector2D(other.x() - x, other.y() - y);
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

    public Vector2D addTo(Vector2D other) {
        return new Vector2D(other.x + x, other.y + y);
    }

    public Vector2D scale(double scalar) {
        return new Vector2D(x * scalar, y * scalar);
    }

    public Vector2D normalized() {
        return scale(1 / magnitude());
    }

    public static Vector2D ZERO() {
        return new Vector2D(0, 0);
    }
}
