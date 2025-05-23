package de.adamyan.antsimulation.Physics;

public final class Utils {
    private Utils() {
        throw new UnsupportedOperationException();
    }

    public static boolean inBounds(double[] pos, double[] bound1, double[] bound2) {
        boolean xInBounds = inBounds(pos[0], Math.min(bound1[0], bound2[0]), Math.max(bound1[0], bound2[0]));
        boolean yInBounds = inBounds(pos[1], Math.min(bound1[1], bound2[1]), Math.max(bound1[1], bound2[1]));
        return xInBounds && yInBounds;
    }

    public static boolean inBounds(double d, double lowerBound, double higherBound) {
        return lowerBound <= d && d <= higherBound;
    }
}
