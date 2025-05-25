package de.adamyan.antsimulation.Physics;

public final class Utils {
    private Utils() {
        throw new UnsupportedOperationException();
    }

    public static double distSquared(double x, double y) {
        return x * x + y * y;
    }

    public static boolean inBounds(Vector2D pos, Vector2D bound1, Vector2D bound2) {
        boolean xInBounds = inBounds(pos.x(), Math.min(bound1.x(), bound2.x()), Math.max(bound1.x(), bound2.x()));
        boolean yInBounds = inBounds(pos.y(), Math.min(bound1.y(), bound2.y()), Math.max(bound1.y(), bound2.y()));
        return xInBounds && yInBounds;
    }

    public static boolean inBounds(double d, double lowerBound, double higherBound) {
        return lowerBound <= d && d <= higherBound;
    }
}
